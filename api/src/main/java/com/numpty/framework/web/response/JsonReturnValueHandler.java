package com.numpty.framework.web.response;

import com.numpty.framework.web.api.ModelAndView;
import com.numpty.framework.web.MessageConverter;
import com.numpty.framework.web.http.MutableRequest;

import com.numpty.framework.web.http.ServerRequest;
import com.numpty.framework.web.http.ServerResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

public class JsonReturnValueHandler implements ReturnValueHandler {

    private final MessageConverter messageConverter;

    public JsonReturnValueHandler(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    @Override
    public boolean supports(Class<?> returnType) {
        return returnType != void.class
                && returnType != ModelAndView.class
                && returnType != String.class
                && !Map.class.isAssignableFrom(returnType);
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception {
        messageConverter.write(result, response);
        return null;
    }
}
