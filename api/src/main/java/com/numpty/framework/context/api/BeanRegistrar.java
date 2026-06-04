package com.numpty.framework.context.api;

import com.numpty.framework.context.BeanRegistry;
import com.numpty.framework.infra.api.RedisStreamRegistry;

public interface BeanRegistrar {

    void register(BeanRegistry registry, InitialContext initialContext);

    default void registerSecurityConfig(BeanRegistry registry, InitialContext initialContext) {

    }

    default void registerMessageListener(RedisStreamRegistry registry) {

    }
}
