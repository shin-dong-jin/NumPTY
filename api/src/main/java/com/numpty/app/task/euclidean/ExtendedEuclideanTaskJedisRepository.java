package com.numpty.app.task.euclidean;

import com.numpty.app.task.common.entity.ExtendedEuclideanTaskPayload;
import com.numpty.app.task.common.entity.Task;
import com.numpty.app.infra.exception.RepositoryException;
import com.numpty.app.task.euclidean.mapper.ExtendedEuclideanTaskMapper;
import com.numpty.framework.infra.api.JedisPoolProvider;
import com.numpty.framework.web.http.HttpStatus;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.XAddParams;

public class ExtendedEuclideanTaskJedisRepository {

    private static final String STREAM_KEY = "numpty:was:request";
    private final JedisPoolProvider poolProvider;
    private final ExtendedEuclideanTaskMapper taskMapper;

    public ExtendedEuclideanTaskJedisRepository(JedisPoolProvider poolProvider,
        ExtendedEuclideanTaskMapper taskMapper) {
        this.poolProvider = poolProvider;
        this.taskMapper = taskMapper;
    }

    public void xAddStream(Task<ExtendedEuclideanTaskPayload> task) {
        try (Jedis jedis = poolProvider.getJedisResource()) {
            jedis.xadd(
                STREAM_KEY,
                XAddParams.xAddParams().maxLen(1_000).approximateTrimming(),
                taskMapper.toRedisStreamRequestMap(task)
            );
        } catch (Exception e) {
            throw new RepositoryException("Error occurred while XADD stream: " + STREAM_KEY + ", " + e.getMessage(), e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
