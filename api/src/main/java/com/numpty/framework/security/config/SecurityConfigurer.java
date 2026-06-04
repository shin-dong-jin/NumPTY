package com.numpty.framework.security.config;

public interface SecurityConfigurer {

    default void init(HttpSecurity http) {}

    void configure(HttpSecurity http);
}
