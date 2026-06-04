package com.numpty.app.task.gcd.dto;

import com.numpty.app.task.common.Algorithm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public record GcdTaskResultUpdateRequest(
    String id,
    List<Algorithm> algorithms,
    BigInteger gcd,
    BigDecimal elapsedMs
) {

    public GcdTaskResultUpdateRequest {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id required.");
        }

        if (algorithms == null || algorithms.isEmpty()) {
            throw new IllegalArgumentException("algorithms required.");
        }

        if (gcd == null) {
            throw new IllegalArgumentException("gcd required.");
        }

        if (elapsedMs == null) {
            throw new IllegalArgumentException("elapsedMs required.");
        }
    }
}
