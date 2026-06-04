package com.numpty.framework.data.transaction;

import com.numpty.framework.data.ConnectionContext;
import java.sql.Connection;
import java.sql.SQLException;

import com.numpty.framework.data.DatabaseProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);
    private final DatabaseProvider dbProvider;

    public TransactionManager(DatabaseProvider dbProvider) {
        this.dbProvider = dbProvider;
    }

    public void begin() throws SQLException {
        Connection conn = dbProvider.getConnection();
        conn.setAutoCommit(false);
        ConnectionContext.bind(conn);
    }

    public void commit() throws SQLException {
        Connection conn = dbProvider.getConnection();

        if (conn != null) {
            conn.commit();
            cleanup(conn);
        }
    }

    public void rollback() {
        Connection conn = null;

        try {
            conn = dbProvider.getConnection();
            if (conn != null) {
                conn.rollback();
                log.error("Error occurred while transaction, rollback successfully.");
            }
        } catch (SQLException e) {
            log.error("Error occurred while transaction, rollback failed.", e);
        } finally {
            cleanup(conn);
        }
    }

    private void cleanup(Connection conn) {
        if (conn == null) {
            return;
        }

        try {
            ConnectionContext.clear();
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException e) {
            log.error("Error occurred while connection cleanup.", e);
        }
    }
}
