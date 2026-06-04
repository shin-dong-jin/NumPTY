package com.numpty.app.task.common.entity;

import com.numpty.app.task.common.TaskType;
import com.numpty.app.task.common.entity.vo.input.GcdTaskInput;
import com.numpty.app.task.common.entity.vo.output.GcdTaskOutput;

public final class GcdTaskPayload extends TaskPayload<GcdTaskInput, GcdTaskOutput> {

    private GcdTaskPayload(GcdTaskInput input) {
        super(input);
    }

    public static GcdTaskPayload create(GcdTaskInput input) {
        return new GcdTaskPayload(input);
    }

    public static GcdTaskPayload reconstitute(GcdTaskInput input) {
        return new GcdTaskPayload(input);
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.GCD;
    }
}
