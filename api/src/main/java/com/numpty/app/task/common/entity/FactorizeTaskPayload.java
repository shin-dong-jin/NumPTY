package com.numpty.app.task.common.entity;

import com.numpty.app.task.common.TaskType;
import com.numpty.app.task.common.entity.vo.input.FactorizeTaskInput;
import com.numpty.app.task.common.entity.vo.output.FactorizeTaskOutput;

public final class FactorizeTaskPayload extends TaskPayload<FactorizeTaskInput, FactorizeTaskOutput> {

    private FactorizeTaskPayload(FactorizeTaskInput input) {
        super(input);
    }

    public static FactorizeTaskPayload create(FactorizeTaskInput input) {
        return new FactorizeTaskPayload(input);
    }

    public static FactorizeTaskPayload reconstitute(FactorizeTaskInput input) {
        return new FactorizeTaskPayload(input);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.FACTORIZE;
    }
}
