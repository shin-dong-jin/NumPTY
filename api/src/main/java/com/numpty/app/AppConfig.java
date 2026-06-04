package com.numpty.app;

import com.numpty.app.infra.auth.*;
import com.numpty.app.demo.DemoController;
import com.numpty.app.task.euclidean.handler.ExtendedEuclideanTaskWorkerCompletedHandler;
import com.numpty.app.task.euclidean.handler.ExtendedEuclideanTaskWorkerFailedHandler;
import com.numpty.app.task.euclidean.handler.ExtendedEuclideanTaskWorkerUnknownHandler;
import com.numpty.app.task.euclidean.handler.ExtendedEuclideanTaskWorkerWorkingHandler;
import com.numpty.app.task.factorize.handler.FactorizeTaskWorkerCompletedHandler;
import com.numpty.app.task.factorize.handler.FactorizeTaskWorkerFailedHandler;
import com.numpty.app.task.factorize.handler.FactorizeTaskWorkerUnknownHandler;
import com.numpty.app.task.factorize.handler.FactorizeTaskWorkerWorkingHandler;
import com.numpty.app.task.gcd.handler.GcdTaskWorkerCompletedHandler;
import com.numpty.app.task.gcd.handler.GcdTaskWorkerFailedHandler;
import com.numpty.app.task.gcd.handler.GcdTaskWorkerUnknownHandler;
import com.numpty.app.task.gcd.handler.GcdTaskWorkerWorkingHandler;
import com.numpty.app.task.lcm.handler.LcmTaskWorkerCompletedHandler;
import com.numpty.app.task.lcm.handler.LcmTaskWorkerFailedHandler;
import com.numpty.app.task.lcm.handler.LcmTaskWorkerUnknownHandler;
import com.numpty.app.task.lcm.handler.LcmTaskWorkerWorkingHandler;
import com.numpty.app.task.primality.handler.PrimalityTestTaskWorkerCompletedHandler;
import com.numpty.app.task.primality.handler.PrimalityTestTaskWorkerFailedHandler;
import com.numpty.app.task.primality.handler.PrimalityTestTaskWorkerUnknownHandler;
import com.numpty.app.task.primality.handler.PrimalityTestTaskWorkerWorkingHandler;
import com.numpty.app.main.MainController;
import com.numpty.app.task.euclidean.mapper.ExtendedEuclideanTaskMapper;
import com.numpty.app.task.euclidean.mapper.ExtendedEuclideanTaskPayloadMapper;
import com.numpty.app.task.factorize.mapper.FactorizeTaskMapper;
import com.numpty.app.task.factorize.mapper.FactorizeTaskPayloadMapper;
import com.numpty.app.task.gcd.mapper.GcdTaskMapper;
import com.numpty.app.task.gcd.mapper.GcdTaskPayloadMapper;
import com.numpty.app.task.lcm.mapper.LcmTaskMapper;
import com.numpty.app.task.lcm.mapper.LcmTaskPayloadMapper;
import com.numpty.app.task.common.listener.ListenerHandlers;
import com.numpty.app.task.common.listener.WorkerDefaultHandler;
import com.numpty.app.task.common.listener.WorkerStreamListener;
import com.numpty.app.task.primality.mapper.PrimalityTestTaskMapper;
import com.numpty.app.task.primality.mapper.PrimalityTestTaskPayloadMapper;
import com.numpty.app.user.UserController;
import com.numpty.app.task.euclidean.*;
import com.numpty.app.task.factorize.*;
import com.numpty.app.task.gcd.*;
import com.numpty.app.task.lcm.*;
import com.numpty.app.task.common.TaskLifecycleMapper;
import com.numpty.app.task.primality.*;
import com.numpty.app.user.UserRepository;
import com.numpty.app.demo.DemoRouterConfig;
import com.numpty.app.user.UserRouterConfig;
import com.numpty.app.user.UserService;
import com.numpty.framework.context.api.BeanRegistrar;
import com.numpty.framework.context.BeanRegistry;
import com.numpty.framework.context.api.InitialContext;
import com.numpty.app.main.MainRouterConfig;
import com.numpty.framework.infra.api.StreamMessageListener;
import com.numpty.framework.support.JsonMapper;
import com.numpty.framework.security.core.authentication.AuthenticationManager;
import com.numpty.framework.security.core.authentication.AuthenticationProvider;
import com.numpty.framework.security.config.configurer.CsrfConfigurer;
import com.numpty.framework.security.web.chain.SecurityChainOrder;
import com.numpty.framework.security.web.chain.SecurityFilterChain;
import com.numpty.framework.security.config.HttpSecurity;

import java.util.List;

public class AppConfig implements BeanRegistrar {

    @Override
    public void register(BeanRegistry registry, InitialContext context) {
        // Mappers
        TaskLifecycleMapper taskLifecycleMapper = new TaskLifecycleMapper();

        GcdTaskPayloadMapper gcdTaskPayloadMapper = new GcdTaskPayloadMapper();
        GcdTaskMapper gcdTaskMapper = new GcdTaskMapper(taskLifecycleMapper, gcdTaskPayloadMapper);

        LcmTaskPayloadMapper lcmTaskPayloadMapper =  new LcmTaskPayloadMapper();
        LcmTaskMapper lcmTaskMapper = new LcmTaskMapper(taskLifecycleMapper, lcmTaskPayloadMapper);

        ExtendedEuclideanTaskPayloadMapper extendedEuclideanTaskPayloadMapper = new ExtendedEuclideanTaskPayloadMapper();
        ExtendedEuclideanTaskMapper extendedEuclideanTaskMapper = new ExtendedEuclideanTaskMapper(taskLifecycleMapper, extendedEuclideanTaskPayloadMapper);

        PrimalityTestTaskPayloadMapper primalityTestTaskPayloadMapper = new PrimalityTestTaskPayloadMapper();
        PrimalityTestTaskMapper primalityTestTaskMapper = new PrimalityTestTaskMapper(taskLifecycleMapper, primalityTestTaskPayloadMapper);

        FactorizeTaskPayloadMapper factorizeTaskPayloadMapper = new FactorizeTaskPayloadMapper();
        FactorizeTaskMapper factorizeTaskMapper = new FactorizeTaskMapper(taskLifecycleMapper, factorizeTaskPayloadMapper);

        registry.register("gcdTaskMapper", gcdTaskMapper);
        registry.register("lcmTaskMapper", lcmTaskMapper);
        registry.register("extendedEuclideanTaskMapper", extendedEuclideanTaskMapper);
        registry.register("primalityTestTaskMapper", primalityTestTaskMapper);
        registry.register("factorizeTaskMapper", factorizeTaskMapper);



        // Repositories
        UserRepository userRepository = new UserRepository(context.getJdbcTemplate());

        GcdTaskMongoRepository gcdTaskMongoRepository = new GcdTaskMongoRepository(context.getMongoTemplate(), gcdTaskMapper);
        GcdTaskJedisRepository gcdTaskJedisRepository = new GcdTaskJedisRepository(context.getJedisPoolProvider(), gcdTaskMapper);

        LcmTaskMongoRepository lcmTaskMongoRepository = new LcmTaskMongoRepository(context.getMongoTemplate(), lcmTaskMapper);
        LcmTaskJedisRepository lcmTaskJedisRepository = new LcmTaskJedisRepository(context.getJedisPoolProvider(), lcmTaskMapper);

        ExtendedEuclideanTaskMongoRepository extendedEuclideanTaskMongoRepository = new ExtendedEuclideanTaskMongoRepository(context.getMongoTemplate(), extendedEuclideanTaskMapper);
        ExtendedEuclideanTaskJedisRepository extendedEuclideanTaskJedisRepository = new ExtendedEuclideanTaskJedisRepository(context.getJedisPoolProvider(), extendedEuclideanTaskMapper);

        PrimalityTestTaskMongoRepository primalityTestTaskMongoRepository = new PrimalityTestTaskMongoRepository(context.getMongoTemplate(), primalityTestTaskMapper);
        PrimalityTestTaskJedisRepository primalityTestTaskJedisRepository = new PrimalityTestTaskJedisRepository(context.getJedisPoolProvider(), primalityTestTaskMapper);

        FactorizeTaskMongoRepository factorizeTaskMongoRepository = new FactorizeTaskMongoRepository(context.getMongoTemplate(), factorizeTaskMapper);
        FactorizeTaskJedisRepository factorizeTaskJedisRepository = new FactorizeTaskJedisRepository(context.getJedisPoolProvider(), factorizeTaskMapper);

        registry.register("userRepository", userRepository);
        registry.register("gcdTaskMongoRepository", gcdTaskMongoRepository);
        registry.register("gcdTaskJedisRepository", gcdTaskJedisRepository);
        registry.register("lcmTaskMongoRepository", lcmTaskMongoRepository);
        registry.register("lcmTaskJedisRepository", lcmTaskJedisRepository);
        registry.register("extendedEuclideanTaskMongoRepository", extendedEuclideanTaskMongoRepository);
        registry.register("extendedEuclideanTaskJedisRepository", extendedEuclideanTaskJedisRepository);
        registry.register("primalityTestTaskMongoRepository", primalityTestTaskMongoRepository);
        registry.register("primalityTestTaskJedisRepository", primalityTestTaskJedisRepository);
        registry.register("factorizeTaskMongoRepository", factorizeTaskMongoRepository);
        registry.register("factorizeTaskJedisRepository", factorizeTaskJedisRepository);



        // Services
        UserService userService = new UserService(userRepository, context.getPasswordEncoder(), context.getJwtProvider());
        GcdTaskService gcdTaskService = new GcdTaskService(gcdTaskMongoRepository, gcdTaskJedisRepository, gcdTaskMapper);
        LcmTaskService lcmTaskService = new LcmTaskService(lcmTaskMongoRepository, lcmTaskJedisRepository, lcmTaskMapper);
        ExtendedEuclideanTaskService extendedEuclideanTaskService = new ExtendedEuclideanTaskService(extendedEuclideanTaskMongoRepository, extendedEuclideanTaskJedisRepository, extendedEuclideanTaskMapper);
        PrimalityTestTaskService primalityTestTaskService = new PrimalityTestTaskService(primalityTestTaskMongoRepository, primalityTestTaskJedisRepository, primalityTestTaskMapper);
        FactorizeTaskService factorizeTaskService = new FactorizeTaskService(factorizeTaskMongoRepository, factorizeTaskJedisRepository, factorizeTaskMapper);

        registry.register("userService", userService);
        registry.register("gcdTaskService", gcdTaskService);
        registry.register("lcmTaskService", lcmTaskService);
        registry.register("extendedEuclideanTaskService", extendedEuclideanTaskService);
        registry.register("primalityTestTaskService", primalityTestTaskService);
        registry.register("factorizeTaskService", factorizeTaskService);



        // Controllers
        UserController userController = new UserController(userService);
        GcdTaskController gcdTaskController = new GcdTaskController(gcdTaskService);
        LcmTaskController lcmTaskController = new LcmTaskController(lcmTaskService);
        ExtendedEuclideanTaskController extendedEuclideanTaskController = new  ExtendedEuclideanTaskController(extendedEuclideanTaskService);
        PrimalityTestTaskController primalityTestTaskController = new PrimalityTestTaskController(primalityTestTaskService);
        FactorizeTaskController factorizeTaskController = new FactorizeTaskController(factorizeTaskService);

        registry.register("userController", userController);
        registry.register("gcdTaskController", gcdTaskController);
        registry.register("lcmTaskController", lcmTaskController);
        registry.register("extendedEuclideanTaskController", extendedEuclideanTaskController);
        registry.register("primalityTestTaskController", primalityTestTaskController);
        registry.register("factorizeTaskController", factorizeTaskController);



        // App Router config
        UserRouterConfig userRouterConfig = new UserRouterConfig(userController);
        GcdTaskRouterConfig gcdTaskRouterConfig = new GcdTaskRouterConfig(gcdTaskController);
        LcmTaskRouterConfig lcmTaskRouterConfig = new LcmTaskRouterConfig(lcmTaskController);
        ExtendedEuclideanTaskRouterConfig extendedEuclideanTaskRouterConfig = new ExtendedEuclideanTaskRouterConfig(extendedEuclideanTaskController);
        PrimalityTestTaskRouterConfig primalityTestTaskRouterConfig = new PrimalityTestTaskRouterConfig(primalityTestTaskController);
        FactorizeTaskRouterConfig factorizeTaskRouterConfig = new FactorizeTaskRouterConfig(factorizeTaskController);

        registry.register("userRouterConfig", userRouterConfig);
        registry.register("gcdTaskRouterConfig", gcdTaskRouterConfig);
        registry.register("lcmTaskRouterConfig", lcmTaskRouterConfig);
        registry.register("extendedEuclideanTaskRouterConfig", extendedEuclideanTaskRouterConfig);
        registry.register("primalityTestTaskRouterConfig",  primalityTestTaskRouterConfig);
        registry.register("factorizeTaskRouterConfig",  factorizeTaskRouterConfig);



        // Listener Handlers
        ListenerHandlers listenerHandlers = new ListenerHandlers(
                List.of(
                        new GcdTaskWorkerWorkingHandler(gcdTaskService, gcdTaskMapper), new GcdTaskWorkerFailedHandler(gcdTaskService, gcdTaskMapper),
                        new GcdTaskWorkerCompletedHandler(gcdTaskService, gcdTaskMapper), new GcdTaskWorkerUnknownHandler(gcdTaskService, gcdTaskMapper),

                        new LcmTaskWorkerWorkingHandler(lcmTaskService, lcmTaskMapper), new LcmTaskWorkerFailedHandler(lcmTaskService, lcmTaskMapper),
                        new LcmTaskWorkerCompletedHandler(lcmTaskService, lcmTaskMapper), new LcmTaskWorkerUnknownHandler(lcmTaskService, lcmTaskMapper),

                        new ExtendedEuclideanTaskWorkerWorkingHandler(extendedEuclideanTaskService, extendedEuclideanTaskMapper), new ExtendedEuclideanTaskWorkerFailedHandler(extendedEuclideanTaskService, extendedEuclideanTaskMapper),
                        new ExtendedEuclideanTaskWorkerCompletedHandler(extendedEuclideanTaskService, extendedEuclideanTaskMapper), new ExtendedEuclideanTaskWorkerUnknownHandler(extendedEuclideanTaskService, extendedEuclideanTaskMapper),

                        new PrimalityTestTaskWorkerWorkingHandler(primalityTestTaskService, primalityTestTaskMapper), new PrimalityTestTaskWorkerFailedHandler(primalityTestTaskService, primalityTestTaskMapper),
                        new PrimalityTestTaskWorkerCompletedHandler(primalityTestTaskService, primalityTestTaskMapper), new PrimalityTestTaskWorkerUnknownHandler(primalityTestTaskService, primalityTestTaskMapper),

                        new FactorizeTaskWorkerWorkingHandler(factorizeTaskService, factorizeTaskMapper), new FactorizeTaskWorkerFailedHandler(factorizeTaskService, factorizeTaskMapper),
                        new FactorizeTaskWorkerCompletedHandler(factorizeTaskService, factorizeTaskMapper), new FactorizeTaskWorkerUnknownHandler(factorizeTaskService, factorizeTaskMapper),

                        new WorkerDefaultHandler()
                )
        );

        StreamMessageListener streamMessageListener = new WorkerStreamListener(listenerHandlers);
        context.getRedisStreamRegistry().register(streamMessageListener);






        // demo
        DemoController demoController = new DemoController();
        registry.register("demoController", demoController);

        DemoRouterConfig demoRouterConfig = new DemoRouterConfig(demoController);
        registry.register("demoRouterConfig", demoRouterConfig);

        // main
        MainController mainController = new MainController();
        registry.register("mainController", mainController);

        MainRouterConfig mainRouterConfig = new MainRouterConfig(mainController);
        registry.register("mainRouterConfig", mainRouterConfig);
    }

    @Override
    public void registerSecurityConfig(BeanRegistry registry, InitialContext initialContext) {
        // Jwt Authentication Provider
        AuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(initialContext.getJwtProvider());
        registry.register("jwtAuthenticationProvider", jwtAuthenticationProvider);

        // Provider Manager
        AuthenticationManager authenticationManager = initialContext.getAuthenticationManager();
        authenticationManager.addProvider(jwtAuthenticationProvider);

        // Jwt Authentication Filter
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager);

        // Jwt Access Denied Handler
        JwtAccessDeniedHandler accessDeniedHandler = new JwtAccessDeniedHandler(initialContext.getJsonMapper());

        // Jwt Authentication Entry Point
        JwtAuthenticationEntryPoint authenticationEntryPoint = new JwtAuthenticationEntryPoint(initialContext.getJsonMapper());

        // API Filter Chain
        SecurityFilterChain apiFilterChain = new HttpSecurity()
                .setSharedObject(JsonMapper.class, initialContext.getJsonMapper())
                .order(SecurityChainOrder.API)
                .securityMatchers("/**")
                .cors(cors -> cors
                    .allowedOrigins("http://localhost:8080", "http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("Authorization", "Content-Type")
                    .exposedHeaders("X-Total-Count")
                    .allowCredentials(true)
                    .maxAge(3600)
                )
                .csrf(CsrfConfigurer::disable)
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilter(jwtAuthenticationFilter)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health-check").permitAll()
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/register").permitAll()
                        .requestMatchers("/auth/refresh").permitAll()
                        .requestMatchers("/demo/**").permitAll()
                        .anyRequest().authenticated())
                .build();

        registry.register("apiFilterChain", apiFilterChain);
    }
}
