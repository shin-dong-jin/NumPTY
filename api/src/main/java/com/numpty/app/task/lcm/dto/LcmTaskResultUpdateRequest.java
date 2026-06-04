package com.numpty.app.task.lcm.dto;

import com.numpty.app.task.common.Algorithm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public record LcmTaskResultUpdateRequest(
    String id,
    List<Algorithm> algorithms,
    BigInteger lcm,
    BigDecimal elapsedMs
) {

    public LcmTaskResultUpdateRequest {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id required.");
        }

        if (algorithms == null || algorithms.isEmpty()) {
            throw new IllegalArgumentException("algorithms required.");
        }

        if (lcm == null) {
            throw new IllegalArgumentException("lcm required.");
        }

        if (elapsedMs == null) {
            throw new IllegalArgumentException("elapsedMs required.");
        }
    }
}
