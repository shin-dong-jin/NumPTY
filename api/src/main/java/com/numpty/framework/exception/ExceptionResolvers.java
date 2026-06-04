package com.numpty.framework.exception;

import com.numpty.framework.web.api.ErrorResponseBody;
import com.numpty.framework.web.api.Request;
import com.numpty.framework.web.api.ResponseEntity;
import com.numpty.framework.web.http.HttpStatus;
import com.numpty.framework.web.http.MutableRequest;
import com.numpty.framework.web.http.ServerRequest;
import com.numpty.framework.web.http.ServerResponse;
import com.numpty.framework.web.response.ResponseEntityReturnValueHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExceptionResolvers {

    private static final Logger log = LoggerFactory.getLogger(ExceptionResolvers.class);
    private final List<ExceptionResolver> exceptionResolvers;
    private final ResponseEntityReturnValueHandler returnValueHandler;

    public ExceptionResolvers(List<ExceptionResolver> exceptionResolvers, ResponseEntityReturnValueHandler returnValueHandler) {
        this.exceptionResolvers = exceptionResolvers;
        this.returnValueHandler = returnValueHandler;
    }

    public void findAndResolve(Exception exception, HttpServletRequest request, HttpServletResponse response) {
        exceptionResolvers.stream()
                .filter(resolver -> resolver.supports(exception))
                .findFirst()
                .ifPresentOrElse(
                        resolver -> resolver.resolve(exception, request, response),
                        () -> {
                            try {
                                log.error("Unsupported exception occurred: {}", exception.getMessage(), exception);

                                ErrorResponseBody responseBody = new ErrorResponseBody(
                                        HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                                        HttpStatus.INTERNAL_SERVER_ERROR.name(),
                                        request.getRequestURI()
                                );

                                ResponseEntity<ErrorResponseBody> responseEntity = ResponseEntity.internalError().body(responseBody);

                                returnValueHandler.handle(request, response, responseEntity);
                            } catch (Exception e) {
                                try {
                                    log.error("Error occurred while resolving unsupported exception: {}", request.getRequestURI(), e);
                                    response.sendError(
                                            HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                                            HttpStatus.INTERNAL_SERVER_ERROR.name()
                                    );
                                } catch (Exception ex) {
                                    log.error("Unsupported exception resolve network failure: {}", request.getRequestURI(), ex);
                                }
                            }
                        });
    }
}
