export const initSocketServer = (server, handler) => {
  handler(server);

  return server;
};
