package com.numpty.app.task.primality.dto;

import com.numpty.app.task.common.Algorithm;

import java.math.BigDecimal;
import java.util.List;

public record PrimalityTestTaskResultUpdateRequest(
    String id,
    List<Algorithm> algorithms,
    Boolean isPrime,
    BigDecimal elapsedMs
) {

    public PrimalityTestTaskResultUpdateRequest {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id required.");
        }

        if (algorithms == null || algorithms.isEmpty()) {
            throw new IllegalArgumentException("algorithms required.");
        }

        if (isPrime == null) {
            throw new IllegalArgumentException("isPrime required.");
        }

        if (elapsedMs == null) {
            throw new IllegalArgumentException("elapsedMs required.");
        }
    }
}
