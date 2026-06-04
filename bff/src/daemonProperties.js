export const daemonProperties = {
  server: {
    path: "/tmp/numpty.sock",
    maxConnections: 100,
  },
  redis: {
    host: process.env.REDIS_HOST || "localhost",
    port: process.env.REDIS_PORT || 6379,
    password: process.env.REDIS_PASSWORD || undefined,
    streams: {
      request: "numpty:was:request",
      response: "numpty:worker:response",
    },
  },
};
