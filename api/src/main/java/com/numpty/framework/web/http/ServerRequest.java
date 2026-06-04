package com.numpty.framework.web.http;

import com.numpty.framework.context.ApplicationContext;
import com.numpty.framework.web.MessageConverter;
import com.numpty.framework.exception.CoreException;
import com.numpty.framework.web.api.Request;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ServerRequest extends HttpServletRequestWrapper implements Request {

    private static final Logger log = LoggerFactory.getLogger(ServerRequest.class);
    private final Map<String, String> pathVariables = new HashMap<>();
    private MessageConverter messageConverter;

    public ServerRequest(HttpServletRequest req) {
        super(req);
    }

    public static ServerRequest from(ServletRequest request) {
        if (request instanceof ServerRequest) {
            return (ServerRequest) request;
        }

        ServletRequest current = request;
        while (current instanceof HttpServletRequestWrapper wrapper) {
            ServletRequest wrapped = wrapper.getRequest();

            if (wrapped instanceof ServerRequest) {
                return (ServerRequest) wrapped;
            }

            current = wrapped;
        }

        if (request instanceof HttpServletRequest) {
            return new ServerRequest((HttpServletRequest) request);
        }

        throw new IllegalArgumentException("Invalid request type: " + request.getClass());
    }

    @Override
    public RequestParameter getPathVariable(String pathName) {
        return new RequestParameter(pathName, pathVariables.get(pathName));
    }

    @Override
    public RequestParameter getQueryString(String queryName) {
        return new RequestParameter(queryName, getParameter(queryName));
    }

    @Override
    public <T> T convertBodyToDTO(Class<T> clazz) {
        validateJsonRequest();

        return getMessageConverter().read(this, clazz);
    }

    private void validateJsonRequest() {
        HttpMethod method = HttpMethod.fromString(getMethod());

        if (method == null || !method.isBodyRequired()) {
            log.warn("This HTTP method does not support a request body. Data may be lost.");
        }

        String contentType = getContentType();

        if (contentType == null || !contentType.startsWith("application/json")) {
            throw new CoreException("Required headers are missing: " + contentType,
                HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public String path() {
        String uri = getRequestURI();

        return Optional.ofNullable(getContextPath())
            .filter(contextPath -> !contextPath.isBlank())
            .filter(uri::startsWith)
            .map(contextPath -> uri.substring(contextPath.length()))
            .orElse(uri);
    }

    public void putAllPathVariables(Map<String, String> pathVariables) {
        this.pathVariables.putAll(pathVariables);
    }

    private MessageConverter getMessageConverter() {
        if (messageConverter == null) {
            ApplicationContext applicationContext = (ApplicationContext) getServletContext().getAttribute(
                "APPLICATION_CONTEXT");
            messageConverter = applicationContext.getBean(MessageConverter.class);
        }

        return messageConverter;
    }
}
