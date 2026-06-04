package com.numpty.app.task.common;

import com.numpty.app.task.common.entity.TaskLifecycle;

import java.util.Date;
import org.bson.Document;

import java.time.Instant;

public class TaskLifecycleMapper {

    public TaskLifecycle toEntity(Document document) {
        return TaskLifecycle.reconstitute(
            TaskStatus.valueOf(document.getString(TaskKeys.TASK_STATUS.getValue())),
            getInstant(document, TaskKeys.CREATED_AT.getValue()),
            getInstant(document, TaskKeys.UPDATED_AT.getValue()),
            getInstant(document, TaskKeys.COMPLETED_AT.getValue())
        );
    }

    private Instant getInstant(Document document, String key) {
        Date date = document.getDate(key);
        return date != null ? date.toInstant() : null;
    }

    public Document toDocument(TaskLifecycle taskLifecycle) {
        return new Document()
                .append(TaskKeys.TASK_STATUS.getValue(), taskLifecycle.getTaskStatus().name())
                .append(TaskKeys.CREATED_AT.getValue(), taskLifecycle.getCreatedAt())
                .append(TaskKeys.UPDATED_AT.getValue(), taskLifecycle.getUpdatedAt())
                .append(TaskKeys.COMPLETED_AT.getValue(), taskLifecycle.getCompletedAt());
    }
}
