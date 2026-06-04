package com.numpty.framework.data;

import com.numpty.framework.support.AbstractSafeCloseable;
import com.numpty.framework.web.http.HttpStatus;
import com.numpty.framework.exception.CoreException;
import com.numpty.framework.boot.Environment;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseProvider extends AbstractSafeCloseable {

    private static final Logger log = LoggerFactory.getLogger(DatabaseProvider.class);
    private final DataSource dataSource;

    public DatabaseProvider() {
        super("Data Source");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(Environment.getProperty("db.driver"));
        hikariConfig.setJdbcUrl(
                Environment.getProperty("db.url"));
        hikariConfig.setUsername(Environment.getProperty("db.username"));
        hikariConfig.setPassword(Environment.getProperty("db.password"));
        hikariConfig.setMaximumPoolSize(Environment.getPropertyAsInt("db.max_pool_size", 10));
        hikariConfig.setConnectionTimeout(Environment.getPropertyAsInt("db.connection_timeout", 5_000));
        hikariConfig.setMaxLifetime(Environment.getPropertyAsInt("db.max_lifetime", 1_800_000));

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() {
        try {
            Connection conn = ConnectionContext.getConnection();

            if (conn == null) {
                return dataSource.getConnection();
            }

            return conn;
        } catch (SQLException e) {
            throw new CoreException("Error occurred while DB connection.", e,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doClose() {
        if (dataSource == null || !(dataSource instanceof AutoCloseable)) {
            log.info("Data Source is missing, already closed, or not closeable.");
            return;
        }

        try {
            ((AutoCloseable) dataSource).close();
            log.info("Data Source is closed.");
        } catch (Exception e) {
            log.error("Failed to close Data Source.", e);
        }
    }
}
