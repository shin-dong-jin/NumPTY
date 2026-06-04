package com.numpty.framework.data.transaction.api;

import com.numpty.framework.data.transaction.TransactionManager;
import com.numpty.framework.exception.api.BusinessException;
import com.numpty.framework.web.http.HttpStatus;
import com.numpty.framework.exception.CoreException;

public class TransactionTemplate {

    private final TransactionManager txManager;

    public TransactionTemplate(TransactionManager txManager) {
        this.txManager = txManager;
    }

    public <T> T execute(TransactionCallback<T> transaction) {
        try {
            txManager.begin();
            T result = transaction.action();
            txManager.commit();

            return result;
        } catch (Exception e) {
            txManager.rollback();
            throw new CoreException("Transaction error occurs.", e,
                HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
