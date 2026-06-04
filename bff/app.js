import { server, shutdown } from "./src/appContext.js";
import { getLogger } from "./src/logger.js";

const log = getLogger("app", import.meta.url);

const PORT = server.port;

const handleShutdown = () => {
  shutdown().catch((e) => {
    log.error("Shutdown filaed:", e);
    process.exit(1);
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

server.on("error", (err) => {
  log.error("Server error:", err);
  handleShutdown();
});

server.listen(PORT, () => log.info(`Server listening on port ${PORT}...`));
