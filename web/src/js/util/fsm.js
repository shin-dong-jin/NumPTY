export const createFSM = ({
  initial,
  transitions,
  name = "FSM",
  onTransition,
}) => {
  let state = initial;
  const listeners = new Set();

  const notify = (prev, next) => {
    onTransition?.(prev, next);
    listeners.forEach((fn) => fn(next, prev));
  };

  return {
    getState: () => state,

    transition: (next) => {
      const allowed = transitions[state];

      if (!allowed?.includes(next)) {
        throw new Error(`[FSM] Not allowed transition (${state} -> ${next})`);
      }

      const prev = state;
      state = next;
      notify(prev, next);
      return state;
    },

    can: (next) => transitions[state]?.includes(next) ?? false,

    is: (...states) => states.includes(state),

    subscribe: (fn) => {
      listeners.add(fn);
      return () => listeners.delete(fn);
    },
  };
};
