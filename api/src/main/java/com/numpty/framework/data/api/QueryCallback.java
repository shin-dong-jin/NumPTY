package com.numpty.framework.data.api;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface QueryCallback<T> {

    T action(PreparedStatement psmt) throws SQLException;
}
