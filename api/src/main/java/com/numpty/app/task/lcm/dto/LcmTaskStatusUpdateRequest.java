package com.numpty.app.task.lcm.dto;

import com.numpty.app.task.common.TaskStatus;
import com.numpty.app.task.common.TaskType;

public record LcmTaskStatusUpdateRequest(
        String id,
        TaskType taskType,
        TaskStatus taskStatus
) {

    public LcmTaskStatusUpdateRequest {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id required.");
        }

        if (taskType == null) {
            throw new IllegalArgumentException("taskType required.");
        }

        if (taskStatus == null) {
            throw new IllegalArgumentException("taskStatus required.");
        }
    }

}
