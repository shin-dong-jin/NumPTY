package com.numpty.framework.data.api;

import com.numpty.framework.data.ConnectionUtils;
import com.numpty.framework.data.DatabaseProvider;
import com.numpty.framework.exception.CoreException;
import com.numpty.framework.exception.api.BusinessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCTemplate {

    private static final Logger log = LoggerFactory.getLogger(JDBCTemplate.class);
    private final DatabaseProvider databaseProvider;

    public JDBCTemplate(DatabaseProvider databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    public <T> T execute(String sql, QueryCallback<T> query) {
        Connection conn = null;
        PreparedStatement psmt = null;

        try {
            conn = databaseProvider.getConnection();
            psmt = conn.prepareStatement(sql);

            return query.action(psmt);
        } catch (Exception e) {
            throw new CoreException("Error occurred while processing database.", e);
        } finally {
            if (psmt != null) {
                try {
                    psmt.close();
                } catch (SQLException e) {
                    log.error("Error occurred while closing psmt.", e);
                }
            }

            ConnectionUtils.releaseConnection(conn);
        }
    }
}
