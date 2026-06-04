package com.numpty.app.task.common.entity.vo.input;

import com.numpty.app.infra.exception.ArgumentValidationException;

import java.math.BigInteger;

public record GcdTaskInput(BigInteger targetA, BigInteger targetB) {

    public GcdTaskInput {
        if (targetA == null) {
            throw new ArgumentValidationException("targetA required.");
        }

        if (targetB == null) {
            throw new ArgumentValidationException("targetB required.");
        }

        if (targetA.signum() == 0 || targetB.signum() == 0) {
            throw new ArgumentValidationException("targets must not be zero.");
        }

        if (targetA.bitLength() > 1024 || targetB.bitLength() > 1024) {
            throw new ArgumentValidationException("targets too large (max 1024 bits)");
        }
    }
}
