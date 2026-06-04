package com.numpty.app.task.factorize.dto;

import com.numpty.app.task.common.Algorithm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public record FactorizeTaskResultUpdateRequest(
    String id,
    List<Algorithm> algorithms,
    List<BigInteger> factors,
    BigDecimal elapsedMs
) {

    public FactorizeTaskResultUpdateRequest {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id required.");
        }

        if (algorithms == null || algorithms.isEmpty()) {
            throw new IllegalArgumentException("algorithms required.");
        }

        if (factors == null || factors.isEmpty()) {
            throw new IllegalArgumentException("factors required.");
        }

        if (elapsedMs == null) {
            throw new IllegalArgumentException("elapsedMs required.");
        }
    }
}
