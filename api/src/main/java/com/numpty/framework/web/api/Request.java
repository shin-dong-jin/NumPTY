package com.numpty.framework.web.api;

import com.numpty.framework.web.http.RequestParameter;

public interface Request {

    RequestParameter getPathVariable(String pathName);

    RequestParameter getQueryString(String queryName);

    String getHeader(String headerName);

    <T> T convertBodyToDTO(Class<T> clazz);

    String path();
}
