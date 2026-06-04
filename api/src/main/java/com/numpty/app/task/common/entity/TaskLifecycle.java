package com.numpty.app.task.common.entity;

import com.numpty.app.infra.exception.StateValidationException;
import com.numpty.app.task.common.TaskStatus;

import java.time.Instant;

public class TaskLifecycle {

    private TaskStatus taskStatus;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;

    private TaskLifecycle(TaskStatus taskStatus, Instant createdAt, Instant updatedAt, Instant completedAt) {
        this.taskStatus = taskStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    public static TaskLifecycle createNow() {
        return TaskLifecycle.builder()
            .taskStatus(TaskStatus.PENDING)
            .createdAt(Instant.now())
            .build();
    }

    public static TaskLifecycle reconstitute(TaskStatus taskStatus, Instant createdAt, Instant updatedAt, Instant completedAt) {
        return TaskLifecycle.builder()
            .taskStatus(taskStatus)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .completedAt(completedAt)
            .build();
    }

    public void markWorking() {
        if (taskStatus != TaskStatus.PENDING) {
            throw new StateValidationException("Task cannot be marked from " + taskStatus.name() + " to " + TaskStatus.WORKING);
        }

        this.taskStatus = TaskStatus.WORKING;
        this.updatedAt = Instant.now();
    }

    public void markCompleted() {
        if (taskStatus != TaskStatus.WORKING) {
            throw new StateValidationException("Task cannot be marked from " + taskStatus.name() + " to " + TaskStatus.COMPLETED);
        }

        Instant now = Instant.now();

        this.taskStatus = TaskStatus.COMPLETED;
        this.updatedAt = now;
        this.completedAt = now;
    }

    public void markFailed() {
        if (taskStatus != TaskStatus.PENDING && taskStatus != TaskStatus.WORKING) {
            throw new StateValidationException("Task cannot be marked from " + taskStatus.name() + " to " + TaskStatus.FAILED);
        }

        this.taskStatus = TaskStatus.FAILED;
        this.updatedAt = Instant.now();
    }

    public void markUnknown() {
        this.taskStatus = TaskStatus.UNKNOWN;
        this.updatedAt = Instant.now();
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    private static class Builder {

        private TaskStatus taskStatus;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant completedAt;

        public Builder taskStatus(TaskStatus taskStatus) {
            this.taskStatus = taskStatus;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public TaskLifecycle build() {
            return new TaskLifecycle(taskStatus, createdAt, updatedAt, completedAt);
        }
    }

    private static Builder builder() {
        return new Builder();
    }
}
