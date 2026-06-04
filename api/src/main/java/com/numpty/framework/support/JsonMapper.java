package com.numpty.framework.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.numpty.framework.web.http.HttpStatus;
import com.numpty.framework.exception.CoreException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JsonMapper {

    private final ObjectMapper mapper;

    public JsonMapper() {
        this.mapper = com.fasterxml.jackson.databind.json.JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
    }

    public <T> T read(InputStream inputStream, Class<T> type) {
        try {
            return mapper.readValue(inputStream, type);
        } catch (IOException e) {
            throw new CoreException("JSON read parsing error occurs.", e,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void write(OutputStream outputStream, Object object) {
        try {
            mapper.writeValue(outputStream, object);
        } catch (IOException e) {
            throw new CoreException("JSON write parsing error occurs.", e,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public <T> T readFromString(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new CoreException("Json read parsing error occurs.", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String writeAsString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new CoreException("JSON write parsing error occurs.", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
