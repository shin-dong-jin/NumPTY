package com.numpty.app.task.common.entity;

import com.numpty.app.task.common.TaskType;
import com.numpty.app.task.common.entity.vo.input.ExtendedEuclideanTaskInput;
import com.numpty.app.task.common.entity.vo.output.ExtendedEuclideanTaskOutput;

public final class ExtendedEuclideanTaskPayload extends TaskPayload<ExtendedEuclideanTaskInput, ExtendedEuclideanTaskOutput> {

    private ExtendedEuclideanTaskPayload(ExtendedEuclideanTaskInput input) {
        super(input);
    }

    public static ExtendedEuclideanTaskPayload create(ExtendedEuclideanTaskInput input) {
        return new ExtendedEuclideanTaskPayload(input);
    }

    public static ExtendedEuclideanTaskPayload reconstitute(ExtendedEuclideanTaskInput input) {
        return new ExtendedEuclideanTaskPayload(input);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.EXTENDED_GCD;
    }
}
