package com.numpty.framework.data;

import java.sql.Connection;
import java.sql.SQLException;

import com.numpty.framework.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionUtils {

    private static final Logger log = LoggerFactory.getLogger(ConnectionUtils.class);

    private ConnectionUtils() {
        throw new CoreException("Instantiation not allowed for this class.");
    }

    public static void releaseConnection(Connection conn) {
        if (conn == null) {
            return;
        }

        Connection txConn = ConnectionContext.getConnection();

        if (txConn == conn) {
            return;
        }

        try {
            conn.close();
        } catch (SQLException e) {
            log.error("Error occurred while connection close.", e);
        }
    }
}
