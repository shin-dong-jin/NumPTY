package com.numpty.framework.web.handler;

import com.numpty.framework.web.api.Request;

@FunctionalInterface
public interface HandlerFunction {

    Object handle(Request request) throws Exception;
}
