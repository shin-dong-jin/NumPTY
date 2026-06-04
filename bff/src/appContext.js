import { appProperties } from "./appProperties.js";

import { createServer } from "./config/httpConfig.js";
import { createPtyFactory } from "./config/ptyConfig.js";
import { createSessionStore } from "./store/sessionStore.js";
import { createPtyService } from "./service/ptyService.js";
import { createWsHandler } from "./handler/wsHandler.js";
import { startPtyServer } from "./server/ptyServer.js";
import { getLogger } from "./logger.js";
import { printBanner } from "./util/bannerLoader.js";

const log = getLogger("app", import.meta.url);

await printBanner("app.logo", [
  "Web Terminal Starting...",
  "                (v1.0.0)",
]);

const { server: serverProps, pty: ptyProps } = appProperties;

const server = createServer();
server.port = serverProps.port;

const ptyFactory = createPtyFactory(ptyProps);

const sessionStore = createSessionStore();

const serviceFactory = () => createPtyService(sessionStore, ptyFactory);

const wsHandler = createWsHandler(serviceFactory);

const wss = startPtyServer(server, wsHandler);

let isShuttingDown = false;
const shutdown = async () => {
  if (isShuttingDown) {
    return;
  }
  isShuttingDown = true;

  const forceExit = setTimeout(() => {
    log.error("Shutdown timeout. Forcing exit...");
    process.exit(1);
  }, 10000);

  log.info("Shutdown server: Releasing PTY sessions...");

  const safeClosePromises = [...wss.clients].map(
    (ws) =>
      new Promise((resolve) => {
        ws.once("close", resolve);
        ws.ptyService?.terminate();
      }),
  );

  await Promise.all(safeClosePromises);

  log.info("All PTY sessions released.");

  await new Promise((resolve) => wss.close(resolve));

  server.close(() => {
    log.info("HTTP server closed.");
    clearTimeout(forceExit);
    process.exit(0);
  });
};

export { server, shutdown };
