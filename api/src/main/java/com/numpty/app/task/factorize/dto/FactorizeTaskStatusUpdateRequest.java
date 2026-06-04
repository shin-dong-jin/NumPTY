package com.numpty.app.task.factorize.dto;

import com.numpty.app.task.common.TaskStatus;
import com.numpty.app.task.common.TaskType;

public record FactorizeTaskStatusUpdateRequest(
        String id,
        TaskType taskType,
        TaskStatus taskStatus) {

    public FactorizeTaskStatusUpdateRequest {
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
