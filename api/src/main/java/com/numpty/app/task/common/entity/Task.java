package com.numpty.app.task.common.entity;

import com.numpty.app.infra.exception.ArgumentValidationException;
import com.numpty.app.infra.exception.StateValidationException;
import com.numpty.app.task.common.Algorithm;
import com.numpty.app.task.common.TaskType;

import java.util.List;

public class Task<P extends TaskPayload<?, ?>> {

    private String id;

    private String userId;
    private TaskType taskType;
    private List<Algorithm> algorithms;

    private TaskLifecycle taskLifecycle;
    private P taskPayload;

    private Task(String id, String userId, TaskType taskType, List<Algorithm> algorithms, TaskLifecycle taskLifecycle, P taskPayload) {
        this.id = id;
        this.userId = userId;
        this.taskType = taskType;
        this.algorithms = algorithms == null ? List.of() : algorithms;
        this.taskLifecycle = taskLifecycle;
        this.taskPayload = taskPayload;
    }

    public static <P extends TaskPayload<?, ?>> Task<P> create(String id, String userId, P taskPayload) {
        if (id == null || id.isBlank()) {
            throw new ArgumentValidationException("id required.");
        }

        if (userId == null || userId.isBlank()) {
            throw new ArgumentValidationException("userId required.");
        }

        if (taskPayload == null) {
            throw new ArgumentValidationException("taskPayload required.");
        }

        return new Task<>(id, userId, taskPayload.getTaskType(), List.of(), TaskLifecycle.createNow(), taskPayload);
    }

    public static <P extends TaskPayload<?, ?>> Task<P> reconstitute(String id, String userId, TaskType taskType, List<Algorithm> algorithms, TaskLifecycle taskLifecycle, P taskPayload) {
        if (taskPayload.getTaskType() != taskType) {
            throw new ArgumentValidationException("TaskType mismatch. Expected: " + taskPayload + ", Actual: " + taskPayload.getTaskType() + ".");
        }

        return new Task<>(id, userId, taskType, algorithms, taskLifecycle, taskPayload);
    }

    public void assignAlgorithms(List<Algorithm> algorithms) {
        if (this.algorithms != null && !this.algorithms.isEmpty()) {
            throw new StateValidationException("Algorithms already assigned.");
        }

        if (algorithms == null || algorithms.isEmpty()) {
            throw new ArgumentValidationException("algorithms required.");
        }

        this.algorithms = List.copyOf(algorithms);
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public List<Algorithm> getAlgorithms() {
        return algorithms;
    }

    public TaskLifecycle getTaskLifecycle() {
        return taskLifecycle;
    }

    public P getTaskPayload() {
        return taskPayload;
    }
}
