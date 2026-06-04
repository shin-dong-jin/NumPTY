package com.numpty.framework.infra.api;

import org.bson.Document;

@FunctionalInterface
public interface DocumentMapper<T> {

    T map(Document document);
}
