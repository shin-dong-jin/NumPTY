package com.numpty.framework.data.transaction.api;

@FunctionalInterface
public interface TransactionCallback<T> {

    T action() throws Exception;
}
