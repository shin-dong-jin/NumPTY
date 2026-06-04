package com.numpty.app.task.common.listener;

import com.numpty.app.task.common.TaskKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class WorkerDefaultHandler implements ListenerHandler {
    private static final Logger log = LoggerFactory.getLogger(WorkerDefaultHandler.class);

    @Override
    public boolean supports(Map<String, String> data) {
        return true;
    }

    @Override
    public void handle(Map<String, String> data) {
        if (data == null) {
            log.error("Received null data from worker stream.");
            return;
        }

        String taskId = data.get(TaskKeys.TASK_ID.getValue());
        if (taskId == null || taskId.isBlank()) {
            log.error("Missing taskId. Cannot recover. Raw: {}", data);
            return;
        }

        String taskType = data.get(TaskKeys.TASK_TYPE.getValue());
        if (taskType == null || taskType.isBlank()) {
            log.error("Missing taskType for taskId={}. Cannot recover. Raw: {}", taskId, data);
            return;
        }

        String status = data.get(TaskKeys.TASK_STATUS.getValue());
        log.warn("Unhandled message: taskId={}, taskType={}, status={}. Raw: {}", taskId, taskType, status, data);
    }
}
