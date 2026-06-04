package com.numpty.framework.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractSafeCloseable implements AutoCloseable {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final String resourceName;

    public AbstractSafeCloseable(String resourceName) {
        this.resourceName = resourceName == null ? this.getClass().getSimpleName() : resourceName;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            doClose();
            return;
        }

        log.info("{} is already closed.", resourceName);
    }

    public boolean isClosed() {
        return closed.get();
    }

    protected abstract void doClose();
}
