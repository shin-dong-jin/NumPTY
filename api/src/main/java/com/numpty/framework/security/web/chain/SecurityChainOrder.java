package com.numpty.framework.security.web.chain;

public enum SecurityChainOrder {
    API(100), ADMIN(200), PUBLIC(Integer.MAX_VALUE);

    private final int value;

    SecurityChainOrder(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
