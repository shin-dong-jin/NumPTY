package com.numpty.framework.context;

import com.numpty.framework.security.core.authentication.AuthenticationManager;
import com.numpty.framework.security.core.authentication.ProviderManager;
import com.numpty.framework.security.core.crypto.BCryptPasswordEncoder;
import com.numpty.framework.security.core.crypto.PasswordEncoder;
import com.numpty.framework.security.core.jwt.JwtProvider;
import com.numpty.framework.boot.Environment;
import com.numpty.framework.context.api.BeanRegistrar;
import com.numpty.framework.context.api.InitialContext;
import com.numpty.framework.data.DatabaseProvider;
import com.numpty.framework.data.api.JDBCTemplate;
import com.numpty.framework.exception.*;
import com.numpty.framework.infra.RedisStreamRegistry;
import com.numpty.framework.infra.MongoProvider;
import com.numpty.framework.infra.api.MongoTemplate;
import com.numpty.framework.security.web.chain.FilterChainProxy;
import com.numpty.framework.security.web.chain.SecurityFilterChain;
import com.numpty.framework.web.handler.FunctionalHandlerAdapter;
import com.numpty.framework.web.handler.HandlerAdapter;
import com.numpty.framework.web.handler.HandlerAdapters;
import com.numpty.framework.infra.api.JedisPoolProvider;
import com.numpty.framework.web.response.*;
import com.numpty.framework.web.router.RouterFunction;
import com.numpty.framework.web.api.RouterRegistrar;
import com.numpty.framework.web.router.RouterRegistry;
import com.numpty.framework.data.transaction.TransactionManager;
import com.numpty.framework.data.transaction.api.TransactionTemplate;
import com.numpty.framework.support.JsonMapper;
import com.numpty.framework.web.MessageConverter;
import com.numpty.framework.web.view.ViewEngine;
import com.numpty.framework.web.view.ViewResolver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BeanInitializer {

    private BeanInitializer() {
        throw new CoreException("Instantiation not allowed for this class.");
    }

    public static void registerBasicBeans(BeanRegistry registry) {
        // Password Encoder
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        registry.register("passwordEncoder", passwordEncoder);

        // Jwt Provider
        JwtProvider jwtProvider = new JwtProvider(
            Environment.getProperty("jwt.secret"), 86_400_000L);
        registry.register("jwtProvider", jwtProvider);

        // Provider Manager
        AuthenticationManager providerManager = new ProviderManager();
        registry.register("providerManager", providerManager);

        // Jedis Pool Provider
        JedisPoolProvider jedisPoolProvider = new JedisPoolProvider();
        registry.register("jedisPoolProvider", jedisPoolProvider);

        // Redis Stream Registry
        RedisStreamRegistry redisStreamRegistry = new RedisStreamRegistry(jedisPoolProvider);
        registry.register("redisStreamRegistry", redisStreamRegistry);

        // Mongo Provider
        MongoProvider mongoProvider = new MongoProvider();
        registry.register("mongoProvider", mongoProvider);

        // Mongo Template
        MongoTemplate mongoTemplate = new MongoTemplate(mongoProvider);
        registry.register("mongoTemplate", mongoTemplate);

        // Database Provider
        DatabaseProvider databaseProvider = new DatabaseProvider();
        registry.register("databaseProvider", databaseProvider);

        // JDBC Template
        JDBCTemplate jdbcTemplate = new JDBCTemplate(databaseProvider);
        registry.register("jdbcTemplate", jdbcTemplate);

        // Transaction
        TransactionManager txManager = new TransactionManager(databaseProvider);
        TransactionTemplate transactionTemplate = new TransactionTemplate(txManager);
        registry.register("txManager", txManager);
        registry.register("transactionTemplate", transactionTemplate);

        // Json Mapper
        JsonMapper jsonMapper = new JsonMapper();
        registry.register("jsonMapper", jsonMapper);

        // View Resolver
        ViewResolver viewResolver = new ViewResolver("templates/", ".html");
        registry.register("viewResolver", viewResolver);

        // View Engine
        ViewEngine viewEngine = new ViewEngine();
        registry.register("viewEngine", viewEngine);

        // Message Converter
        MessageConverter messageConverter = new MessageConverter(jsonMapper);
        registry.register("messageConverter", messageConverter);

        // ReturnValueHandlers
        ReturnValueHandler viewReturnValueHandler = new ViewReturnValueHandler();
        ReturnValueHandler jsonReturnValueHandler = new JsonReturnValueHandler(messageConverter);
        ReturnValueHandler responseEntityReturnValueHandler = new ResponseEntityReturnValueHandler(
                messageConverter);
        registry.register("viewReturnValueHandler", viewReturnValueHandler);
        registry.register("jsonReturnValueHandler", jsonReturnValueHandler);
        registry.register("responseEntityReturnValueHandler", responseEntityReturnValueHandler);

        // Router Registry
        RouterRegistry routerRegistry = new RouterRegistry();
        registry.register("routerRegistry", routerRegistry);
    }

    public static InitialContext createInitialBeans(ApplicationContext context) {
        return new CoreContext(
                context.getBean(PasswordEncoder.class),
                context.getBean(JwtProvider.class),
                context.getBean(AuthenticationManager.class),
                context.getBean(JsonMapper.class),
                context.getBean(TransactionTemplate.class),
                context.getBean(JDBCTemplate.class),
                context.getBean(MongoTemplate.class),
                context.getBean(JedisPoolProvider.class),
                context.getBean(RedisStreamRegistry.class)
        );
    }

    public static void registerCustomBeans(BeanRegistry registry, BeanRegistrar registrar,
                                           InitialContext initialContext) {
        registrar.register(registry, initialContext);
    }

    public static void registerSecurityBeans(BeanRegistry registry, BeanRegistrar registrar, InitialContext initialContext) {
        registrar.registerSecurityConfig(registry, initialContext);
    }

    public static void assemble(ApplicationContext context) {
        // Filter Chain Proxy
        FilterChainProxy filterChainProxy = new FilterChainProxy(
                context.getBeansOfType(SecurityFilterChain.class).values().stream().sorted(Comparator.comparing(SecurityFilterChain::getOrder)).toList()
        );
        context.register("securityFilterChain", filterChainProxy);

        // Main Router
        RouterRegistry registry = context.getBean(RouterRegistry.class);
        RouterFunction router = context.getBeansOfType(RouterRegistrar.class).values().stream()
                .map(registrar -> registrar.configure(registry))
                .reduce(RouterFunction::and)
                .orElse(request -> null);
        context.register("router", router);

        // Return Value Handlers
        ReturnValueHandlers returnValueHandlers = new ReturnValueHandlers(
                new ArrayList<>(context.getBeansOfType(ReturnValueHandler.class).values()));
        context.register("returnValueHandlers", returnValueHandlers);

        // Handler Adapters
        HandlerAdapter functionalHandlerAdapter = new FunctionalHandlerAdapter(returnValueHandlers);
        context.register("functionalHandlerAdapter", functionalHandlerAdapter);

        HandlerAdapters handlerAdapters = new HandlerAdapters(
                new ArrayList<>(context.getBeansOfType(HandlerAdapter.class).values()));
        context.register("handlerAdapters", handlerAdapters);

        // Exception Resolvers
        ExceptionResolver coreExceptionResolver = new CoreExceptionResolver(
                context.getBean(ResponseEntityReturnValueHandler.class));
        ExceptionResolver businessExceptionResolver = new BusinessExceptionResolver(
                context.getBean(ResponseEntityReturnValueHandler.class));
        context.register("coreExceptionResolver", coreExceptionResolver);
        context.register("businessExceptionResolver", businessExceptionResolver);

        ExceptionResolvers exceptionResolvers = new ExceptionResolvers(
                List.copyOf(context.getBeansOfType(ExceptionResolver.class).values()),
                context.getBean(ResponseEntityReturnValueHandler.class));
        context.register("exceptionResolvers", exceptionResolvers);
    }
}
