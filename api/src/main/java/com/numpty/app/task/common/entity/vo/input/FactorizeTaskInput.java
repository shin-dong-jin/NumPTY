package com.numpty.app.task.common.entity.vo.input;

import com.numpty.app.infra.exception.ArgumentValidationException;

import java.math.BigInteger;

public record FactorizeTaskInput(BigInteger target) {

    public FactorizeTaskInput {
        if (target == null) {
            throw new ArgumentValidationException("target required.");
        }

        if (target.compareTo(BigInteger.TWO) < 0) {
            throw new ArgumentValidationException("target must not be less than 2");
        }

        if (target.toString().length() > 30) {
            throw new ArgumentValidationException("target length must be less than 30");
        }
    }
}
