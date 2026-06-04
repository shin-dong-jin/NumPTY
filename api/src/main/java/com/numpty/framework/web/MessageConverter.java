package com.numpty.framework.web;

import com.numpty.framework.support.JsonMapper;
import com.numpty.framework.web.http.HttpStatus;
import com.numpty.framework.exception.CoreException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class MessageConverter {

    private final JsonMapper jsonMapper;

    public MessageConverter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public <T> T read(HttpServletRequest req, Class<T> returnType) {
        try {
            return jsonMapper.read(req.getInputStream(), returnType);
        } catch (IOException e) {
            throw new CoreException("Request input stream error occurs.", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void write(Object object, HttpServletResponse resp) {
        try {
            resp.setContentType("application/json;charset=UTF-8");
            jsonMapper.write(resp.getOutputStream(), object);
        } catch (IOException e) {
            throw new CoreException("Response output stream error occurs.", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
