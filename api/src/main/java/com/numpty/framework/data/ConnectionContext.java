package com.numpty.framework.data;

import java.sql.Connection;

public class ConnectionContext {

    private static final ThreadLocal<Connection> CONNECTION_HOLDER = new ThreadLocal<>();

    public static void bind(Connection connection) {
        CONNECTION_HOLDER.set(connection);
    }

    public static Connection getConnection() {
        return CONNECTION_HOLDER.get();
    }

    public static void clear() {
        CONNECTION_HOLDER.remove();
    }
}
