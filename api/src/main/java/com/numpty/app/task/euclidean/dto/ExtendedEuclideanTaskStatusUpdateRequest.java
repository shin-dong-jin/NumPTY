package com.numpty.app.task.euclidean.dto;

import com.numpty.app.task.common.TaskStatus;
import com.numpty.app.task.common.TaskType;

public record ExtendedEuclideanTaskStatusUpdateRequest(
        String id,
        TaskType taskType,
        TaskStatus taskStatus
) {

    public ExtendedEuclideanTaskStatusUpdateRequest {
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
