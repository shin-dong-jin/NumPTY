import fs from "fs";

import { socketServer, redisListener } from "./src/daemonContext.js";
import { getLogger } from "./src/logger.js";

const log = getLogger("daemon", import.meta.url);

const SOCKET_PATH = socketServer.socketPath;

const handleShutdown = () => {
  log.info("Shutdown server: releasing resources...");

  socketServer.close(() => {
    if (fs.existsSync(SOCKET_PATH)) {
      fs.unlinkSync(SOCKET_PATH);
      log.info("Socket file cleaned up.");
    }

    log.info("Socket server is closed.");
    process.exit(0);
  });
};

process.on("SIGINT", handleShutdown);
process.on("SIGTERM", handleShutdown);
process.on("uncaughtException", (err) => {
  log.error("Uncaught exception:", err);
  handleShutdown();
});
process.on("unhandledRejection", (reason) => {
  log.error("Unhandled rejection:", reason);
  handleShutdown();
});

socketServer.listen(SOCKET_PATH, () => {
  log.info(`Socket Server is listening on: ${SOCKET_PATH}`);

  log.info("Redis Listener is active.");
  redisListener().catch((err) => {
    log.error("Redis Listener Fatal Error:", err);
  });
});
