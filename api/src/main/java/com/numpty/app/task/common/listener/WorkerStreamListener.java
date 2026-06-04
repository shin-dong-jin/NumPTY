package com.numpty.app.task.common.listener;

import com.numpty.framework.infra.api.StreamMessageListener;
import com.numpty.app.task.common.TaskKeys;
import com.numpty.app.infra.exception.ListenerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WorkerStreamListener implements StreamMessageListener {
    private static final Logger log = LoggerFactory.getLogger(WorkerStreamListener.class);
    private static final String RESPONSE_STREAM_KEY = "numpty:worker:response";
    private static final String CONSUMER_GROUP = "cg-was";
    private final ListenerHandlers listenerHandlers;

    public WorkerStreamListener(ListenerHandlers listenerHandlers) {
        this.listenerHandlers = listenerHandlers;
    }

    @Override
    public void onMessage(String streamKey, Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            log.error("Received null or empty data from redis stream: {}", streamKey);
            return;
        }

        String taskId = data.get(TaskKeys.TASK_ID.getValue());
        if (taskId == null || taskId.isBlank()) {
            log.error("Missing Task ID in payload. Dropping message. key: {}, raw data: {}", streamKey, data);
            return;
        }

        try {
            ListenerHandler listenerHandler = listenerHandlers.findListenerHandler(data);
            listenerHandler.handle(data);
        } catch (Exception e) {
            throw new ListenerException("Failed to process stream message. ID: " + taskId + ", Error: " + e.getMessage() + ".", e);
        }
    }

    @Override
    public String getStreamKeyName() {
        return RESPONSE_STREAM_KEY;
    }

    @Override
    public String getConsumerGroupName() {
        return CONSUMER_GROUP;
    }
}
