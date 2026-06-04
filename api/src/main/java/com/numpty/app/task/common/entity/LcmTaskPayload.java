package com.numpty.app.task.common.entity;

import com.numpty.app.task.common.TaskType;
import com.numpty.app.task.common.entity.vo.input.LcmTaskInput;
import com.numpty.app.task.common.entity.vo.output.LcmTaskOutput;

public final class LcmTaskPayload extends TaskPayload<LcmTaskInput, LcmTaskOutput> {

    private LcmTaskPayload(LcmTaskInput input) {
        super(input);
    }

    public static LcmTaskPayload create(LcmTaskInput input) {
        return new LcmTaskPayload(input);
    }

    public static LcmTaskPayload reconstitute(LcmTaskInput input) {
        return new LcmTaskPayload(input);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.LCM;
    }
}
