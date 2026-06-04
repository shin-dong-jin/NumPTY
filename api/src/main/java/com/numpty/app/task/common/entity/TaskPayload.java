package com.numpty.app.task.common.entity;

import com.numpty.app.infra.exception.ArgumentValidationException;
import com.numpty.app.infra.exception.StateValidationException;
import com.numpty.app.task.common.TaskType;

public abstract sealed class TaskPayload<I, O> permits GcdTaskPayload, LcmTaskPayload, ExtendedEuclideanTaskPayload, PrimalityTestTaskPayload, FactorizeTaskPayload {

    private final I input;
    private O output;

    protected TaskPayload(I input) {
        if (input == null) {
            throw new ArgumentValidationException("Input required.");
        }

        this.input = input;
    }

    public I getInput() {
        return this.input;
    }

    public O getOutput() {
        return this.output;
    }

    public void applyOutput(O output) {
        if (this.output != null) {
            throw new StateValidationException("Output already applied for " + getClass().getSimpleName());
        }

        this.output = output;
    }

    public abstract TaskType getTaskType();
}
