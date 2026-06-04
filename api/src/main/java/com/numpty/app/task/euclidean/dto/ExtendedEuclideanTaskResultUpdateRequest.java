package com.numpty.app.task.euclidean.dto;

import com.numpty.app.task.common.Algorithm;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public record ExtendedEuclideanTaskResultUpdateRequest(
        String id,
        List<Algorithm> algorithms,
        BigInteger gcd,
        BigInteger x,
        BigInteger y,
        BigDecimal elapsedMs
) {

    public ExtendedEuclideanTaskResultUpdateRequest {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id required.");
        }

        if (algorithms == null || algorithms.isEmpty()) {
            throw new IllegalArgumentException("algorithms required.");
        }

        if (gcd == null) {
            throw new IllegalArgumentException("gcd required.");
        }

        if (x == null) {
            throw new IllegalArgumentException("x required.");
        }

        if (y == null) {
            throw new IllegalArgumentException("y required.");
        }

        if (elapsedMs == null) {
            throw new IllegalArgumentException("elapsedMs required.");
        }
    }
}
