package com.numpty.app.task.common;

public enum TaskStatus {
    PENDING, WORKING, COMPLETED, FAILED, UNKNOWN;

    public static TaskStatus fromString(String status) {
        try {
            return TaskStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            return UNKNOWN;
        }
    }
}
