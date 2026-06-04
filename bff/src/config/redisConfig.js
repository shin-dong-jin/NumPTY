import Redis from "ioredis";

import { getLogger } from "../logger.js";

const log = getLogger("daemon", import.meta.url);

export const createRedisClient = ({
  host: redisHost,
  port: redisPort,
  password: redisPassword,
}) => {
  const redis = new Redis({
    host: redisHost,
    port: redisPort,
    password: redisPassword,
  });

  redis.on("error", (err) => {
    log.error("Redis Connection Error: ", err);
  });

  redis.on("connect", () => {
    log.info(`Redis Client connected to ${redisHost}:${redisPort}`);
  });

  return redis;
};
