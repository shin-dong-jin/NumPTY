package com.numpty.framework.boot;

import com.numpty.framework.infra.RedisStreamRegistry;
import com.numpty.framework.web.DispatcherServlet;
import com.numpty.framework.context.ApplicationContext;
import com.numpty.framework.context.api.BeanRegistrar;
import com.numpty.framework.data.DatabaseProvider;
import com.numpty.framework.exception.BootstrapException;
import com.numpty.framework.exception.CoreException;
import com.numpty.framework.data.SchemaInitializer;
import com.numpty.framework.security.web.chain.DelegatingFilterProxy;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void run(Class<? extends BeanRegistrar> appConfigClass) {
        Application app = new Application();
        app.start(appConfigClass);
    }

    private void start(Class<? extends BeanRegistrar> appConfigClass) {
        try (ApplicationContext appContext = new ApplicationContext()) {
            long duration = System.nanoTime();

            new BannerPrinter().printBanner();

            BeanRegistrar config = appConfigClass.getDeclaredConstructor().newInstance();
            appContext.init(config);
            appContext.registerShutdownHook();
            initializeSchema(appContext);

            Tomcat tomcat = configureAndStartTomcat(appContext);

            startBackgroundListeners(appContext);

            duration = (System.nanoTime() - duration) / 1_000_000L;
            log.info("Application started successfully in {}ms.", duration);

            tomcat.getServer().await();
        } catch (CoreException e) {
            log.error("Application failed to start: {}", e.getMessage(), e);
            throw new BootstrapException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unknown fatal error occurred, application terminated: {}", e.getMessage(), e);
            throw new BootstrapException(e.getMessage(), e);
        }
    }

    private Tomcat configureAndStartTomcat(ApplicationContext appContext) {
        try {
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(Environment.getServerPort());
            tomcat.getConnector();
            Context context = tomcat.addContext(Environment.getProperty("server.servlet.context-path"), new File(".").getAbsolutePath());

            context.getServletContext().setAttribute("APPLICATION_CONTEXT", appContext);

            FilterDef filterDef = new FilterDef();
            filterDef.setFilterName("securityFilterChain");
            filterDef.setFilter(new DelegatingFilterProxy("securityFilterChain", appContext));
            context.addFilterDef(filterDef);

            FilterMap filterMap = new FilterMap();
            filterMap.setFilterName("securityFilterChain");
            filterMap.addURLPattern("/*");
            context.addFilterMap(filterMap);

            DispatcherServlet dispatcher = new DispatcherServlet(appContext);
            Tomcat.addServlet(context, "dispatcher", dispatcher);
            context.addServletMappingDecoded("/", "dispatcher");

            tomcat.start();
            return tomcat;
        } catch (Exception e) {
            throw new CoreException("Failed to start Tomcat server: " + e.getMessage(), e);
        }
    }

    private void startBackgroundListeners(ApplicationContext appContext) {
        RedisStreamRegistry streamRegistry = appContext.getBean(RedisStreamRegistry.class);
        streamRegistry.initializeGroup();
        streamRegistry.startListening();
    }

    private void initializeSchema(ApplicationContext appContext) {
        boolean initSchema = Boolean.parseBoolean(Environment.getProperty("db.init.schema"));
        String schemaFile = Environment.getProperty("db.init.schema.location");

        if (!initSchema) {
            return;
        }

        SchemaInitializer.executeSchema(appContext.getBean(DatabaseProvider.class), schemaFile);
    }
}
