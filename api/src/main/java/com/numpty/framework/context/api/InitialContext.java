package com.numpty.framework.context.api;

import com.numpty.framework.security.core.jwt.JwtProvider;
import com.numpty.framework.data.api.JDBCTemplate;
import com.numpty.framework.infra.api.JedisPoolProvider;
import com.numpty.framework.data.transaction.api.TransactionTemplate;
import com.numpty.framework.infra.api.MongoTemplate;
import com.numpty.framework.infra.api.RedisStreamRegistry;
import com.numpty.framework.support.JsonMapper;
import com.numpty.framework.security.core.authentication.AuthenticationManager;
import com.numpty.framework.security.core.crypto.PasswordEncoder;

public interface InitialContext {

    PasswordEncoder getPasswordEncoder();

    JwtProvider getJwtProvider();

    AuthenticationManager getAuthenticationManager();

    JsonMapper getJsonMapper();

    TransactionTemplate getTransactionTemplate();

    JDBCTemplate getJdbcTemplate();

    JedisPoolProvider getJedisPoolProvider();

    MongoTemplate getMongoTemplate();

    RedisStreamRegistry getRedisStreamRegistry();
}
