import { getLogger } from "../logger.js";

const log = getLogger("daemon", import.meta.url);

const getLastId = async (redis, streamName) => {
  try {
    const result = await redis.xrevrange(streamName, "+", "-", "COUNT", 1);

    if (result && result.length > 0) {
      return result[0][0];
    }

    return `${Date.now()}-0`;
  } catch (err) {
    return `${Date.now()}-0`;
  }
};

export const startRedisListener = async (redis, handler, streams) => {
  const { request: requestStream, response: responseStream } = streams;

  let requestLastId = await getLastId(redis, requestStream);
  let responseLastId = await getLastId(redis, responseStream);

  while (true) {
    try {
      const streams = await redis.xread(
        "BLOCK",
        0,
        "STREAMS",
        requestStream,
        responseStream,
        requestLastId,
        responseLastId,
      );

      if (!streams) {
        continue;
      }

      for (const [streamName, messages] of streams) {
        for (const [id, fields] of messages) {
          const lastId = handler(streamName, id, fields);

          if (streamName === requestStream) {
            requestLastId = lastId;
          } else if (streamName === responseStream) {
            responseLastId = lastId;
          }
        }
      }
    } catch (err) {
      log.error("Redis Listener Error: ", err);
      await new Promise((res) => setTimeout(res, 1000));
    }
  }
};
