package com.numpty.app.task.common.entity.vo.output;

import com.numpty.app.infra.exception.ArgumentValidationException;

import java.math.BigDecimal;
import java.math.BigInteger;

public record GcdTaskOutput(BigInteger gcd, BigDecimal elapsedMs) {

    public GcdTaskOutput {
        if (gcd == null) {
            throw new ArgumentValidationException("gcd required.");
        }

        if (elapsedMs == null) {
            throw new ArgumentValidationException("elapsedMs required.");
        }

        if (elapsedMs.signum() < 0) {
            throw new ArgumentValidationException("elapsedMs must not be negative.");
        }
    }
}
