package com.numpty.framework.context;

import com.numpty.framework.security.core.jwt.JwtProvider;
import com.numpty.framework.context.api.InitialContext;
import com.numpty.framework.data.api.JDBCTemplate;
import com.numpty.framework.infra.api.JedisPoolProvider;
import com.numpty.framework.data.transaction.api.TransactionTemplate;
import com.numpty.framework.infra.api.MongoTemplate;
import com.numpty.framework.infra.api.RedisStreamRegistry;
import com.numpty.framework.support.JsonMapper;
import com.numpty.framework.security.core.authentication.AuthenticationManager;
import com.numpty.framework.security.core.crypto.PasswordEncoder;

public class CoreContext implements InitialContext {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final JsonMapper jsonMapper;
    private final TransactionTemplate transactionTemplate;
    private final JDBCTemplate jdbcTemplate;
    private final MongoTemplate mongoTemplate;
    private final JedisPoolProvider jedisPoolProvider;
    private final RedisStreamRegistry redisStreamRegistry;

    public CoreContext(PasswordEncoder passwordEncoder, JwtProvider jwtProvider,
        AuthenticationManager authenticationManager, JsonMapper jsonMapper,
        TransactionTemplate transactionTemplate, JDBCTemplate jdbcTemplate,
        MongoTemplate mongoTemplate,
        JedisPoolProvider jedisPoolProvider, RedisStreamRegistry redisStreamRegistry) {
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
        this.jsonMapper = jsonMapper;
        this.transactionTemplate = transactionTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.mongoTemplate = mongoTemplate;
        this.jedisPoolProvider = jedisPoolProvider;
        this.redisStreamRegistry = redisStreamRegistry;
    }

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }

    @Override
    public JwtProvider getJwtProvider() {
        return this.jwtProvider;
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        return this.authenticationManager;
    }

    @Override
    public JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    @Override
    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    @Override
    public JDBCTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public JedisPoolProvider getJedisPoolProvider() {
        return jedisPoolProvider;
    }

    @Override
    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public RedisStreamRegistry getRedisStreamRegistry() {
        return redisStreamRegistry;
    }
}
