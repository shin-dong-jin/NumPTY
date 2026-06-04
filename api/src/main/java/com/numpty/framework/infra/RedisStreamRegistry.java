package com.numpty.framework.infra;

import com.numpty.framework.infra.api.JedisPoolProvider;
import com.numpty.framework.infra.api.StreamMessageListener;
import com.numpty.framework.support.AbstractSafeCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RedisStreamRegistry extends AbstractSafeCloseable implements com.numpty.framework.infra.api.RedisStreamRegistry {
    private static final Logger log = LoggerFactory.getLogger(RedisStreamRegistry.class);
    private final JedisPoolProvider poolProvider;
    private final List<RedisStreamManager> managers = new ArrayList<>();

    public RedisStreamRegistry(JedisPoolProvider poolProvider) {
        super("Redis Stream Registry");
        this.poolProvider = poolProvider;
    }

    @Override
    public void register(StreamMessageListener messageListener) {
        managers.add(new RedisStreamManager(poolProvider, messageListener));
    }

    @Override
    public void registerAll(List<StreamMessageListener> messageListeners) {
        for (StreamMessageListener messageListener : messageListeners) {
            managers.add(new RedisStreamManager(poolProvider, messageListener));
        }
    }

    public void initializeGroup() {
        for (RedisStreamManager manager : managers) {
            manager.initializeGroup();
        }
    }

    public void startListening() {
        for (RedisStreamManager manager : managers) {
            manager.startListening();
        }
    }

    @Override
    protected void doClose() {
        log.info("Releasing {} Worker Threads of Redis Stream Managers...", managers.size());

        for (RedisStreamManager manager : managers) {
            try {
                manager.doClose();
            } catch (Exception e) {
                log.error("Failed to close Redis Stream Manager: {}", manager.getClass().getSimpleName(), e);
            }
        }
    }
}
