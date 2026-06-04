package com.numpty.app.task.euclidean.dto;

import java.math.BigInteger;

public record ExtendedEuclideanTaskCreateRequest(
        String _id,
        BigInteger targetA,
        BigInteger targetB
) {

    public ExtendedEuclideanTaskCreateRequest {
        if (_id == null || _id.isBlank()) {
            throw new IllegalArgumentException("_id required.");
        }

        if (targetA == null) {
            throw new IllegalArgumentException("targetA required.");
        }

        if (targetB == null) {
            throw new IllegalArgumentException("targetB required.");
        }

        if (targetA.bitLength() > 1024 || targetB.bitLength() > 1024) {
            throw new IllegalArgumentException("targets too large (max 1024 bits)");
        }
    }
}
