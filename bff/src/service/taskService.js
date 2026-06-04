export const createTaskService = (store) => {
  const finalizeTask = (_id, client, data, sendResponse) => {
    sendResponse(client.socket, data);

    const isFinished = ["COMPLETED", "FAILED"].includes(data.status);

    if (isFinished) {
      if (client.timeoutId) {
        clearTimeout(client.timeoutId);
        client.socket.end();
        store.remove(_id);
      }
    }
  };

  return {
    onClientRequest: (_id, socket, sendResponse) => {
      if (store.exists(_id)) {
        store.remove(_id);
      }

      const timeoutId = setTimeout(() => {
        const client = store.get(_id);

        if (client) {
          finalizeTask(
            _id,
            client,
            { _id, status: "FAILED", message: "Worker timeout (30s)." },
            sendResponse,
          );
        }
      }, 30000);

      store.add(_id, { socket, timeoutId });
    },

    onWorkerResponse: (taskData, sendResponse) => {
      const client = store.get(taskData._id);

      if (!client) {
        return;
      }

      finalizeTask(taskData._id, client, taskData, sendResponse);
    },
  };
};
