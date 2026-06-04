package com.numpty.framework.infra.api;

import java.util.List;

public interface RedisStreamRegistry {
    void register(StreamMessageListener messageListener);

    void registerAll(List<StreamMessageListener> messageListeners);
}
