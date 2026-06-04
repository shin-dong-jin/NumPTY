import { createFSM } from "../util/fsm";
import { wsUrl } from "../util/url";

export const createService = ({ maxRetry = 3 } = {}) => {
  let socket = null;
  let retryCount = 0;
  let abortController = null;

  const stateFSM = createFSM({
    name: "Connection",
    initial: "DISCONNECTED",
    transitions: {
      DISCONNECTED: ["CONNECTING"],
      CONNECTING: ["CONNECTED", "RECONNECTING", "DISCONNECTED"],
      CONNECTED: ["RECONNECTING", "DISCONNECTED"],
      RECONNECTING: ["CONNECTED", "DISCONNECTED"],
    },
  });

  const openSocket = () =>
    new Promise((resolve, reject) => {
      const ws = new WebSocket(wsUrl("/ws"));
      ws.onopen = () => resolve(ws);
      ws.onerror = (err) => reject(err);
    });

  const send = (type, payload = {}) => {
    if (socket?.readyState !== WebSocket.OPEN) {
      return;
    }

    socket.send(JSON.stringify({ type, ...payload }));
  };

  const connect = async () => {
    stateFSM.transition("CONNECTING");
    socket = await openSocket();
    stateFSM.transition("CONNECTED");
    return socket;
  };

  const reconnect = async (onAttempt) => {
    abortController?.abort();
    abortController = new AbortController();
    const { signal } = abortController;

    stateFSM.transition("RECONNECTING");

    while (retryCount < maxRetry) {
      if (signal.aborted) {
        throw new Error("Reconnect aborted.");
      }

      const delay = 3000 * (1 << retryCount++);
      onAttempt?.({ delay, retryCount, maxRetry });

      await new Promise((resolve) => setTimeout(resolve, delay));

      if (signal.aborted) {
        throw new Error("Reconnect aborted.");
      }

      try {
        socket = await openSocket();
        retryCount = 0;
        abortController = null;
        stateFSM.transition("CONNECTED");
        return socket;
      } catch {
        continue;
      }
    }

    abortController = null;
    stateFSM.transition("DISCONNECTED");
    throw new Error("Failed to reconnect.");
  };

  const destroy = () => {
    abortController?.abort();
    abortController = null;

    if (socket?.readyState === WebSocket.OPEN) {
      socket.close();
    }
    socket = null;
    retryCount = 0;

    if (stateFSM.can("DISCONNECTED")) {
      stateFSM.transition("DISCONNECTED");
    }
  };

  return {
    connect,
    reconnect,
    send,
    destroy,
    getState: () => stateFSM.getState(),
    subscribe: (fn) => stateFSM.subscribe(fn),
  };
};
