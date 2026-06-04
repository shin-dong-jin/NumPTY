package com.numpty.framework.exception;

import com.numpty.framework.exception.api.BusinessException;
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

public class BusinessExceptionResolver implements ExceptionResolver {

    private static final Logger log = LoggerFactory.getLogger(BusinessExceptionResolver.class);
    private final ResponseEntityReturnValueHandler returnValueHandler;

    public BusinessExceptionResolver(ResponseEntityReturnValueHandler returnValueHandler) {
        this.returnValueHandler = returnValueHandler;
    }

    @Override
    public boolean supports(Exception exception) {
        return exception instanceof BusinessException;
    }

    @Override
    public void resolve(Exception exception, HttpServletRequest request, HttpServletResponse response) {
        try {
            BusinessException businessException = (BusinessException) exception;

            printException(businessException);

            ErrorResponseBody responseBody = new ErrorResponseBody(
                    businessException.getStatus().getCode(),
                    businessException.getStatus().name(),
                    request.getRequestURI()
            );

            ResponseEntity<ErrorResponseBody> responseEntity = ResponseEntity.status(businessException.getStatus()).body(responseBody);

            returnValueHandler.handle(request, response, responseEntity);
        } catch (Exception e) {
            try {
                log.error("Error occurred while resolving service exception: {}", request.getRequestURI(), e);

                response.sendError(
                        HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                        HttpStatus.INTERNAL_SERVER_ERROR.name()
                );
            } catch (Exception ex) {
                log.error("Service exception resolver network failure: {}", request.getRequestURI(), ex);
            }
        }
    }

    private void printException(BusinessException e) {
        HttpStatus status = e.getStatus();


        if (status.isError()) {
            log.error(e.getMessage(), e);
            return;
        }

        if (status.isWarn()) {
            log.warn("[{}] {}", status.name(), e.getMessage());
            return;
        }

        log.info(e.getMessage());
    }
}
