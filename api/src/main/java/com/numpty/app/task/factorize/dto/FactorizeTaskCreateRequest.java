package com.numpty.app.task.factorize.dto;

import java.math.BigInteger;

public record FactorizeTaskCreateRequest(
        String _id,
        BigInteger target
) {

    public FactorizeTaskCreateRequest {
        if (_id == null || _id.isBlank()) {
            throw new IllegalArgumentException("_id required.");
        }

        if (target == null) {
            throw new IllegalArgumentException("target required.");
        }

        if (target.bitLength() > 1024) {
            throw new IllegalArgumentException("targets too large (max 1024 bits)");
        }
    }
}
