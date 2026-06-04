package com.numpty.app.task.common.entity.vo.output;

import com.numpty.app.infra.exception.ArgumentValidationException;

import java.math.BigDecimal;
import java.math.BigInteger;

public record ExtendedEuclideanTaskOutput(BigInteger gcd, BigInteger x, BigInteger y, BigDecimal elapsedMs) {

    public ExtendedEuclideanTaskOutput {
        if (gcd == null) {
            throw new ArgumentValidationException("gcd required.");
        }

        if (x == null) {
            throw new ArgumentValidationException("x required.");
        }

        if (y == null) {
            throw new ArgumentValidationException("y required.");
        }

        if (elapsedMs == null) {
            throw new ArgumentValidationException("elapsedMs required.");
        }

        if (elapsedMs.signum() < 0) {
            throw new ArgumentValidationException("elapsedMs must not be negative.");
        }
    }
}
