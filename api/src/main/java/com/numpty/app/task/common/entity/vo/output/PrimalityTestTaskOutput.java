package com.numpty.app.task.common.entity.vo.output;

import com.numpty.app.infra.exception.ArgumentValidationException;

import java.math.BigDecimal;

public record PrimalityTestTaskOutput(Boolean isPrime, BigDecimal elapsedMs) {

    public PrimalityTestTaskOutput {
        if (isPrime == null) {
            throw new ArgumentValidationException("isPrime required.");
        }

        if (elapsedMs == null) {
            throw new ArgumentValidationException("elapsedMs required.");
        }

        if (elapsedMs.signum() < 0) {
            throw new ArgumentValidationException("elapsedMs must not be negative.");
        }
    }
}
