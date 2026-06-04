package com.numpty.framework.web.handler;

import com.numpty.framework.context.ApplicationContext;
import com.numpty.framework.web.http.MutableRequest;
import com.numpty.framework.web.http.ServerRequest;
import com.numpty.framework.web.http.ServerResponse;
import com.numpty.framework.web.response.ReturnValueHandler;
import com.numpty.framework.web.response.ReturnValueHandlers;
import com.numpty.framework.web.api.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FunctionalHandlerAdapter implements HandlerAdapter {

    private final ReturnValueHandlers returnValueHandlers;

    public FunctionalHandlerAdapter(ReturnValueHandlers returnValueHandlers) {
        this.returnValueHandlers = returnValueHandlers;
    }

    @Override
    public boolean supports(Object object) {
        return HandlerFunction.class.isAssignableFrom(object.getClass());
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ServerRequest serverRequest = ServerRequest.from(request);
        HandlerFunction handlerFunction = (HandlerFunction) handler;

        Object result = handlerFunction.handle(serverRequest);

        Class<?> returnType = result == null ? void.class : result.getClass();

        ReturnValueHandler returnValueHandler = returnValueHandlers.findReturnValueHandler(returnType);

        return returnValueHandler.handle(request, response, result);
    }
}
