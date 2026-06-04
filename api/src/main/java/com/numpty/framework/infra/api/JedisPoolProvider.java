package com.numpty.framework.infra.api;

import com.numpty.framework.boot.Environment;
import com.numpty.framework.support.AbstractSafeCloseable;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolProvider extends AbstractSafeCloseable {

    private static final Logger log = LoggerFactory.getLogger(JedisPoolProvider.class);
    private final JedisPool jedisPool;

    public JedisPoolProvider() {
        super("Jedis Pool");

        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(Environment.getPropertyAsInt("redis.pool.max_connection", 10));
        jedisPoolConfig.setMaxIdle(Environment.getPropertyAsInt("redis.pool.max_idle", 5));

        this.jedisPool = Optional.ofNullable(Environment.getProperty("redis.password"))
            .filter(password -> !password.isBlank())
            .map(password -> new JedisPool(
                jedisPoolConfig,
                Environment.getProperty("redis.host"),
                Environment.getPropertyAsInt("redis.port", 6379),
                Environment.getPropertyAsInt("redis.timeout", 2_000),
                password))
            .orElseGet(() -> new JedisPool(
                jedisPoolConfig,
                Environment.getProperty("redis.host"),
                Environment.getPropertyAsInt("redis.port", 6379),
                Environment.getPropertyAsInt("redis.timeout", 2_000)
            ));
    }

    public Jedis getJedisResource() {
        return jedisPool.getResource();
    }

    @Override
    protected void doClose() {
        if (jedisPool == null || jedisPool.isClosed()) {
            log.info("Jedis Pool is either missing or already closed.");
            return;
        }

        try {
            jedisPool.close();
            log.info("Jedis Pool is closed.");
        } catch (Exception e) {
            log.error("Failed to close Jedis Pool.", e);
        }
    }
}
