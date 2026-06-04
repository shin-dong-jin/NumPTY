import net from "net";
import fs from "fs";

import { getLogger } from "../logger.js";

const log = getLogger("daemon", import.meta.url);

export const createSocketServer = ({ path, maxConnections }) => {
  const server = net.createServer();

  if (fs.existsSync(path)) {
    fs.unlinkSync(path);
  }

  server.on("error", (err) => {
    log.error("Socket Server Internal Error: ", err);
  });

  server.maxConnections = maxConnections;

  return server;
};
