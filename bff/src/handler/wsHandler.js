import { getLogger } from "../logger.js";

const log = getLogger("app", import.meta.url);

const setUpMessageInteractions = (ws, ptyService) => {
  ws.on("message", (data) => {
    try {
      const packet = JSON.parse(data.toString());

      switch (packet.type) {
        case "init":
          ptyService.spawn();

          ptyService.onOutput((data) => {
            if (ws.readyState === WebSocket.OPEN) {
              ws.send(data);
            }
          });

          ptyService.onExit(({ exitCode, signal }) => {
            if (ws.readyState === WebSocket.OPEN) {
              ws.close(signal > 0 ? 4006 : 1000);
            }
          });
          break;
        case "stdin":
          ptyService.write(packet.payload);
          break;
        case "resize":
          ptyService.resize(packet.cols, packet.rows);
          break;
        default:
          log.warn("Unknown packet type: ", packet);
      }
    } catch {
      log.error("Invalid protocol format.");
    }
  });

  ws.on("close", () => {
    ptyService.terminate();
  });

  ws.on("error", (err) => {
    log.error("Websocket error: ", err);
    ptyService.terminate();
  });
};

export const createWsHandler = (serviceFactory) => (ws) => {
  const ptyService = serviceFactory();
  ws.ptyService = ptyService;
  setUpMessageInteractions(ws, ptyService);
};
