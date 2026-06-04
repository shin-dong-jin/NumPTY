export const createClientStore = () => {
  const waitingClients = new Map();

  return {
    add: (_id, clientData) => waitingClients.set(_id, clientData),
    get: (_id) => waitingClients.get(_id),
    remove: (_id) => waitingClients.delete(_id),
    exists: (_id) => waitingClients.has(_id),
  };
};
