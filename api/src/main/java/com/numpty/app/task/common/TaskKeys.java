package com.numpty.app.task.common;

public enum TaskKeys {
    TASK_ID("_id"), USER_ID("userId"), TASK_TYPE("type"), ALGORITHMS("algorithms"), TASK_LIFECYCLE("lifecycle"), TASK_PAYLOAD("payload"),
    TASK_STATUS("status"), CREATED_AT("createdAt"), UPDATED_AT("updatedAt"), COMPLETED_AT("completedAt"),
    INPUT("input"), OUTPUT("output"), TARGET("target"), TARGET_A("targetA"), TARGET_B("targetB"), TARGETS("targets"), RESULT("result"), RESULT_X("resultX"), RESULT_Y("resultY"), ELAPSED_MS("elapsedMs");

    private final String value;

    TaskKeys(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
