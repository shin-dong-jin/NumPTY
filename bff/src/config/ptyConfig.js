import pty from "node-pty";

export const createPtyFactory = ({ file, args, options }) => {
  return {
    spawn: () => pty.spawn(file, args, options),
  };
};
