import { parseStreamFields } from "../util/taskParser.js";

export const createRedisHandler = (service) => {
  return (streamName, id, fields) => {
    const taskData = parseStreamFields(fields);

    service.onWorkerResponse(taskData, (socket, data) => {
      socket.write(JSON.stringify(data) + "\n");
    });

    return id;
  };
};
