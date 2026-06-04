package com.numpty.app.task.common.entity;

import com.numpty.app.task.common.TaskType;
import com.numpty.app.task.common.entity.vo.input.PrimalityTestTaskInput;
import com.numpty.app.task.common.entity.vo.output.PrimalityTestTaskOutput;

public final class PrimalityTestTaskPayload extends
    TaskPayload<PrimalityTestTaskInput, PrimalityTestTaskOutput> {

    private PrimalityTestTaskPayload(PrimalityTestTaskInput input) {
        super(input);
    }

    public static PrimalityTestTaskPayload create(PrimalityTestTaskInput input) {
        return new PrimalityTestTaskPayload(input);
    }

    public static PrimalityTestTaskPayload reconstitute(PrimalityTestTaskInput input) {
        return new PrimalityTestTaskPayload(input);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.PRIMALITY_TEST;
    }
}
