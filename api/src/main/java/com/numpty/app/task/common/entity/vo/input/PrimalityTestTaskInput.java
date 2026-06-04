package com.numpty.app.task.common.entity.vo.input;

import com.numpty.app.infra.exception.ArgumentValidationException;

import java.math.BigInteger;

public record PrimalityTestTaskInput(BigInteger target) {

    public PrimalityTestTaskInput {
        if (target == null) {
            throw new ArgumentValidationException("target required.");
        }

        if (target.compareTo(BigInteger.TWO) < 0) {
            throw new ArgumentValidationException("target must not be less than 2");
        }

        if (target.bitLength() > 1024) {
            throw new ArgumentValidationException("target too large (max 1024 bits)");
        }
    }
}
