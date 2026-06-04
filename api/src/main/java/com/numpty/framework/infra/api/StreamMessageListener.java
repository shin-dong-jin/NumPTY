package com.numpty.framework.infra.api;

import java.util.Map;

public interface StreamMessageListener {

    String getStreamKeyName();
    
    String getConsumerGroupName();

    void onMessage(String streamKey, Map<String, String> data);
}
