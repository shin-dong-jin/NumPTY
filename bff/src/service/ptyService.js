import { v4 as uuidv4 } from "uuid";

import { getLogger } from "../logger.js";

const log = getLogger("app", import.meta.url);

export const createPtyService = (sessionStore, ptyFactory) => {
  const sessionId = uuidv4();
  let ptyInstance = null;

  return {
    spawn: () => {
      if (ptyInstance) {
        return;
      }

      ptyInstance = ptyFactory.spawn();
      sessionStore.add(sessionId, ptyInstance);
      log.info(`Session connected: ${sessionId}`);
    },

    getSessionId: () => sessionId,

    onOutput: (callback) => ptyInstance?.onData(callback),

    onExit: (callback) => ptyInstance?.onExit(callback),

    write: (data) => ptyInstance?.write(data),

    resize: (cols, rows) => ptyInstance?.resize(cols, rows),

    terminate: () => {
      if (!ptyInstance) {
        return;
      }

      ptyInstance?.kill();
      ptyInstance = null;
      sessionStore.remove(sessionId);

      log.info(`Session closed: ${sessionId}`);
    },
  };
};
