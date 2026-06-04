import { getLogger } from "../logger.js";

const log = getLogger("daemon", import.meta.url);

export const createSocketHandler = (service) => {
  return (server) => {
    server.on("connection", (socket) => {
      socket.on("data", (data) => {
        const _id = data.toString().trim();

        if (!_id) {
          return;
        }

        service.onClientRequest(_id, socket, (s, responseData) => {
          s.write(JSON.stringify(responseData) + "\n");
        });
      });

      socket.on("error", (err) => {
        log.error(`Socket error: ${err.message}`);
      });
    });
  };
};
