package com.numpty.app.demo;

import java.math.BigInteger;

public class DemoDTO {
    private String id;
    private String message;
    private BigInteger integer;

    public DemoDTO() {
    }

    public DemoDTO(String id, String message, BigInteger integer) {
        this.id = id;
        this.message = message;
        this.integer = integer;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public BigInteger getInteger() {
        return integer;
    }
}
