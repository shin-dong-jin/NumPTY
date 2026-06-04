package com.numpty.app.task.common.entity.vo.output;

import com.numpty.app.infra.exception.ArgumentValidationException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public record FactorizeTaskOutput(List<BigInteger> factors, BigDecimal elapsedMs) {

    public FactorizeTaskOutput {
        if (factors == null || factors.isEmpty()) {
            throw new ArgumentValidationException("factors required.");
        }

        if (elapsedMs == null) {
            throw new ArgumentValidationException("elapsedMs required.");
        }

        if (elapsedMs.signum() < 0) {
            throw new ArgumentValidationException("elapsedMs must not be negative.");
        }

        factors = List.copyOf(factors);
    }
}
