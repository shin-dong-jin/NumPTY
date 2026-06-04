package com.numpty.framework.infra;

import com.numpty.framework.infra.api.JedisPoolProvider;
import com.numpty.framework.infra.api.StreamMessageListener;
import com.numpty.framework.support.AbstractSafeCloseable;
import com.numpty.framework.exception.BootstrapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.resps.StreamEntry;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

public class RedisStreamManager extends AbstractSafeCloseable {

    private static final Logger log = LoggerFactory.getLogger(RedisStreamManager.class);
    private final JedisPoolProvider poolProvider;
    private final StreamMessageListener messageListener;
    private final String streamKey;
    private final String groupName;
    private final String threadName;
    private final String consumerName;
    private Thread workerThread;

    public RedisStreamManager(JedisPoolProvider poolProvider, StreamMessageListener messageListener) {
        super("Redis Stream");
        this.poolProvider = poolProvider;
        this.messageListener = messageListener;

        this.streamKey = messageListener.getStreamKeyName();
        this.groupName = messageListener.getConsumerGroupName();
        this.threadName = "RedisStreamWorker-" + streamKey;
        this.consumerName = generateConsumerName();
    }

    private String generateConsumerName() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();

            long pid = ProcessHandle.current().pid();

            return hostname + ":" + pid + ":" + threadName;
        } catch (Exception e) {
            return "unknown:" + ProcessHandle.current().pid() + ":" + threadName;
        }
    }

    public void initializeGroup() {
        try (Jedis jedis = poolProvider.getJedisResource()) {
            jedis.xgroupCreate(streamKey, groupName, StreamEntryID.XGROUP_LAST_ENTRY, true);
            log.info("Redis Stream Group {} created successfully on {}.", groupName, streamKey);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.debug("Redis Stream Group {} already exists on {}. Skip stream group create.", groupName, streamKey);
            } else {
                throw new BootstrapException("Failed to create Redis Stream Group " + groupName + " on " + streamKey + ".", e);
            }
        }
    }

    public synchronized void startListening() {
        if (isClosed() || workerThread != null) {
            log.warn("Cannot start listening. Manager is closed or already running.");
            return;
        }

        workerThread = new Thread(() -> {
            try {
                while (!isClosed() && !Thread.currentThread().isInterrupted()) {
                    try (Jedis jedis = poolProvider.getJedisResource()) {
                        List<Map.Entry<String, List<StreamEntry>>> results = jedis.xreadGroup(
                                groupName,
                                consumerName,
                                XReadGroupParams.xReadGroupParams().block(2000).count(10),
                                Map.of(streamKey, StreamEntryID.XREADGROUP_UNDELIVERED_ENTRY)
                        );

                        if (results != null) {
                            for (Map.Entry<String, List<StreamEntry>> stream : results) {
                                for (StreamEntry entry : stream.getValue()) {
                                    processEntry(jedis, entry);
                                }
                            }
                        }
                    } catch (JedisConnectionException | JedisDataException e) {
                        if (isClosed()) {
                            break;
                        }

                        log.warn("Redis Stream {} connection lost. Retry in 1s... ({})", streamKey, e.getMessage());
                        safeSleep(1_000L);
                    } catch (Exception e) {
                        log.error("Unexpected error occurred in Redis Stream Manager.", e);
                        if (isClosed()) {
                            break;
                        }
                    }
                }
            } finally {
                log.info("Redis Stream Manager stopped successfully.");
            }
        }, threadName);

        workerThread.start();
    }

    private void processEntry(Jedis jedis, StreamEntry entry) {
        try {
            messageListener.onMessage(streamKey, entry.getFields());
            jedis.xack(streamKey, groupName, entry.getID());
        } catch (Exception e) {
            log.error("Failed to process message id={}, data={}", entry.getID(), entry.getFields(), e);
            jedis.xack(streamKey, groupName, entry.getID());
        }
    }

    private void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.info("Redis stream worker {} received shutdown signal while sleeping. Interrupt thread...", consumerName);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void doClose() {
        if (workerThread == null || !workerThread.isAlive()) {
            log.info("Worker Thread is missing or already interrupted.");
            return;
        }

        try {
            workerThread.interrupt();
            log.info("Worker Thread is interrupted.");
        } catch (Exception e) {
            log.error("Failed to interrupt worker thread: {}", consumerName, e);
        }
    }
}
