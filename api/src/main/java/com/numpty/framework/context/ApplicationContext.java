package com.numpty.framework.context;

import com.numpty.framework.context.api.BeanRegistrar;
import com.numpty.framework.context.api.InitialContext;
import com.numpty.framework.web.http.HttpStatus;
import com.numpty.framework.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApplicationContext implements BeanRegistry, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ApplicationContext.class);
    private final Map<String, Object> beans = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public void init(BeanRegistrar registrar) {
        BeanInitializer.registerBasicBeans(this);

        InitialContext initialContext = BeanInitializer.createInitialBeans(this);

        BeanInitializer.registerCustomBeans(this, registrar, initialContext);

        BeanInitializer.registerSecurityBeans(this, registrar, initialContext);

        BeanInitializer.assemble(this);
    }

    public void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close, "shutdown"));
    }

    @Override
    public void register(String name, Object bean) {
        beans.put(name, bean);
    }

    public <T> T getBean(String name, Class<T> requiredType) {
        Object bean = beans.get(name);

        if (bean == null) {
            throw new CoreException("Retrieved bean is null: " + name, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (!requiredType.isInstance(bean)) {
            throw new CoreException("Bean type mismatch: Expected " + requiredType.getSimpleName() + ", but found " + name + ".",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return requiredType.cast(bean);
    }

    public <T> T getBean(Class<T> requiredType) {
        List<T> found = beans.values().stream()
                .filter(requiredType::isInstance)
                .map(requiredType::cast)
                .toList();

        if (found.isEmpty()) {
            throw new CoreException("Retrieved bean is null: " + requiredType.getSimpleName(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (found.size() > 1) {
            String duplicated = String.join(",", found.stream().map(bean -> bean.getClass().getSimpleName()).toList());

            throw new CoreException("Retrieved bean is duplicated: " + duplicated, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return found.get(0);
    }

    public <T> Map<String, T> getBeansOfType(Class<T> type) {
        Map<String, T> result = new HashMap<>();

        beans.entrySet().stream()
                .filter(entry -> type.isInstance(entry.getValue()))
                .forEach(entry -> result.put(entry.getKey(), type.cast(entry.getValue())));

        return result;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            log.info("Shutdown server: Releasing resources...");

            Map<String, AutoCloseable> closeableBeans = getBeansOfType(AutoCloseable.class);

            closeableBeans.values().forEach(bean -> {
                try {
                    log.info("Releasing resource: {}", bean.getClass().getSimpleName());
                    bean.close();
                } catch (Exception e) {
                    log.error("Error occurred while closing resource: {}", e.getMessage());
                }
            });

            log.info("All resources are released successfully.");
            return;
        }

        log.info("All resources are already released.");
    }

    public boolean isClosed() {
        return closed.get();
    }
}
