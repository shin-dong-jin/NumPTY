export const createSessionStore = () => {
  const sessions = new Map();

  return {
    add: (id, ptyService) => sessions.set(id, ptyService),

    get: (id) => sessions.get(id),

    remove: (id) => {
      const session = sessions.get(id);

      if (session) {
        session.kill();
        sessions.delete(id);
      }
    },

    exists: (id) => sessions.has(id),

    clearAll: () => {
      for (const [id, session] of sessions.entries()) {
        session.kill();
      }
      sessions.clear();
    },
  };
};
