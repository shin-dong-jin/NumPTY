package com.numpty.app.task.gcd;

import com.numpty.app.task.common.entity.GcdTaskPayload;
import com.numpty.app.task.common.entity.Task;
import com.numpty.app.infra.exception.RepositoryException;
import com.numpty.app.task.gcd.mapper.GcdTaskMapper;
import com.numpty.framework.infra.api.JedisPoolProvider;
import com.numpty.framework.web.http.HttpStatus;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.XAddParams;

public class GcdTaskJedisRepository {

    private static final String STREAM_KEY = "numpty:was:request";
    private final JedisPoolProvider poolProvider;
    private final GcdTaskMapper taskMapper;

    public GcdTaskJedisRepository(JedisPoolProvider poolProvider, GcdTaskMapper taskMapper) {
        this.poolProvider = poolProvider;
        this.taskMapper = taskMapper;
    }

    public void xAddStream(Task<GcdTaskPayload> task) {
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
