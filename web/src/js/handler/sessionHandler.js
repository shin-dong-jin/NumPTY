import { createFSM } from "../util/fsm";

export const createSessionHandler = (service, terminal) => {
  const {
    term,
    container,
    fit,
    proposeDimensions,
    updateViewportMarker,
    applyTheme,
  } = terminal;

  const lifecycleFSM = createFSM({
    name: "Session",
    initial: "BOOTING",
    transitions: {
      BOOTING: ["RUNNING", "DISPOSING"],
      RUNNING: ["RECOVERING", "DISPOSING"],
      RECOVERING: ["RUNNING", "DISPOSING"],
      DISPOSING: ["DISPOSED"],
      DISPOSED: [],
    },
  });

  const bindSocket = (socket) => {
    socket.onmessage = (event) => handleWrite(event);

    socket.onclose = (event) => handleDisconnect(event);
  };

  term.onData((data) => {
    if (!lifecycleFSM.is("RUNNING")) {
      return;
    }

    service.send("stdin", { payload: data });
  });

  const resizeObserver = new ResizeObserver(() => handleResize());

  const handleResize = () => {
    if (lifecycleFSM.is("DISPOSED", "DISPOSING")) {
      return;
    }

    try {
      updateViewportMarker();
      fit();
      syncSize();
    } catch (err) {
      console.error("Resize failed:", err);
    }
  };

  const syncSize = () => {
    if (!lifecycleFSM.is("RUNNING")) {
      return;
    }

    const dims = proposeDimensions();
    if (dims) {
      service.send("resize", { cols: dims.cols, rows: dims.rows });
    }
  };

  const handleWrite = (event) => {
    if (lifecycleFSM.is("DISPOSED", "DISPOSING")) {
      return;
    }

    let match;
    let cleaned = event.data;
    const oscRe = /\x1b\]9999;(.+?)\x07/gs;

    while ((match = oscRe.exec(event.data)) != null) {
      const oscData = JSON.parse(match[1]);

      if (oscData.type === "theme") {
        applyTheme(oscData.theme);
      } else if (oscData.type === "token") {
        localStorage.setItem("token", oscData.token);
      } else if (oscData.type === "logout") {
        localStorage.removeItem("token");
      }

      cleaned = cleaned.replace(match[0], "");
    }

    if (cleaned) {
      term.write(cleaned);
    }
  };

  const handleDisconnect = (event) => {
    if (!lifecycleFSM.is("RUNNING")) {
      return;
    }

    switch (event.code) {
      case 1006:
      case 4006:
        lifecycleFSM.transition("RECOVERING");

        term.write("\r\n\x1b[31;1m Connection lost\x1b[0m\r\n");

        service
          .reconnect(({ delay, retryCount, maxRetry }) => {
            term.write(
              `Reconnecting in ${delay / 1000}s... (${retryCount}/${maxRetry})\r\n`,
            );
          })
          .then((socket) => {
            bindSocket(socket);
            service.send("init");
            lifecycleFSM.transition("RUNNING");
            syncSize();
          })
          .catch(() => {
            term.write("\r\nFailed to reconnect. Please refresh.\r\n");
            cleanup();
          });
        break;
      default:
        term.write("\r\n Connection closed.\r\n");
        cleanup();
        break;
    }
  };

  const cleanup = () => {
    if (!lifecycleFSM.can("DISPOSING")) {
      return;
    }

    lifecycleFSM.transition("DISPOSING");

    resizeObserver.disconnect();
    service.destroy();

    lifecycleFSM.transition("DISPOSED");
  };

  const start = async () => {
    fit();

    try {
      const socket = await service.connect();
      bindSocket(socket);
      service.send("init");
      resizeObserver.observe(container);
      lifecycleFSM.transition("RUNNING");
    } catch {
      term.write("\r\n\x1b[31;1m Connection failed\x1b[0m\r\n");

      service
        .reconnect(({ delay, retryCount, maxRetry }) => {
          term.write(
            `Reconnecting in ${delay / 1000}s... (${retryCount}/${maxRetry})\r\n`,
          );
        })
        .then((socket) => {
          bindSocket(socket);
          service.send("init");
          resizeObserver.observe(container);
          lifecycleFSM.transition("RUNNING");
        })
        .catch(() => {
          term.write("\r\nFailed to reconnect. Please refresh.\r\n");
          cleanup();
        });
    }
  };

  return {
    start,
    dispose: () => {
      cleanup();
      terminal.dispose();
    },
    getState: () => lifecycleFSM.getState(),
    subscribe: (fn) => lifecycleFSM.subscribe(fn),
  };
};
