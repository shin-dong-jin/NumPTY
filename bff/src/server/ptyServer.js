import { WebSocketServer } from "ws";

export const startPtyServer = (httpServer, onConnection) => {
  const wss = new WebSocketServer({ server: httpServer });

  wss.on("connection", (ws) => {
    onConnection(ws);
  });

  return wss;
};
