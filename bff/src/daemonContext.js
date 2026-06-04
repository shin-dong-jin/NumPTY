import { daemonProperties } from "./daemonProperties.js";

import { createSocketServer } from "./config/socketConfig.js";
import { createRedisClient } from "./config/redisConfig.js";
import { createClientStore } from "./store/clientStore.js";
import { createTaskService } from "./service/taskService.js";
import { createSocketHandler } from "./handler/socketHandler.js";
import { createRedisHandler } from "./handler/redisHandler.js";
import { initSocketServer } from "./server/socketServer.js";
import { startRedisListener } from "./server/redisListener.js";
import { printBanner } from "./util/bannerLoader.js";

await printBanner("daemon.logo", [
  "Redis Daemon Starting...",
  "                (v1.0.0)",
]);

const { server: serverProps, redis: redisProps } = daemonProperties;

const server = createSocketServer(serverProps);
const redis = createRedisClient(redisProps);

const clientStore = createClientStore();

const taskService = createTaskService(clientStore);

const socketHandler = createSocketHandler(taskService);
const redisHandler = createRedisHandler(taskService);

const socketServer = initSocketServer(server, socketHandler);
socketServer.socketPath = serverProps.path;

const redisListener = () =>
  startRedisListener(redis, redisHandler, redisProps.streams);

export { socketServer, redisListener };
