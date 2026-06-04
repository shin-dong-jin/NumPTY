package com.numpty.framework.web.response;

import com.numpty.framework.web.api.ModelAndView;
import com.numpty.framework.web.api.ResponseEntity;
import com.numpty.framework.web.MessageConverter;
import com.numpty.framework.web.http.MutableRequest;
import com.numpty.framework.web.http.ServerRequest;
import com.numpty.framework.web.http.ServerResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ResponseEntityReturnValueHandler implements ReturnValueHandler {

    private final MessageConverter messageConverter;

    public ResponseEntityReturnValueHandler(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    @Override
    public boolean supports(Class<?> returnType) {
        return ResponseEntity.class.isAssignableFrom(returnType);
    }

    @Override
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object result) throws Exception {
        ResponseEntity<?> entity = (ResponseEntity<?>) result;

        response.setStatus(entity.getStatus().getCode());
        entity.getHeaders().forEach(response::setHeader);

        if (entity.getBody() != null) {
            messageConverter.write(entity.getBody(), response);
        }

        return null;
    }
}
