package com.numpty.app.task.primality.dto;

import java.math.BigInteger;

public record PrimalityTestTaskCreateRequest(
        String _id,
        BigInteger target
) {

    public PrimalityTestTaskCreateRequest {
        if (_id == null || _id.isBlank()) {
            throw new IllegalArgumentException("_id required.");
        }

        if (target == null) {
            throw new IllegalArgumentException("target required.");
        }
    }
}
