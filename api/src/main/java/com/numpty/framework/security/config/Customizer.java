package com.numpty.framework.security.config;

@FunctionalInterface
public interface Customizer<T> {

    void customize(T t);

    static <T> Customizer<T> withDefaults() {
        return t -> {};
    }
}
