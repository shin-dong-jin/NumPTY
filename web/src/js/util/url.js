export const wsUrl = (path) => {
  const wsProtocol = location.protocol === "https:" ? "wss:" : "ws:";

  return `${wsProtocol}//${location.host}${path}`;
};
