package com.numpty.app.user;

import com.numpty.framework.data.api.JDBCTemplate;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class UserRepository {

    private final JDBCTemplate jdbcTemplate;

    public UserRepository(JDBCTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveUser(User user) {
        String sql = "INSERT INTO users (id, email, name, password, status, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.execute(sql, psmt -> {
            psmt.setString(1, user.getId().toString());
            psmt.setString(2, user.getEmail());
            psmt.setString(3, user.getName());
            psmt.setString(4, user.getPassword());
            psmt.setString(5, user.getStatus().toString());
            psmt.setString(6, user.getRole().toString());
            psmt.setTimestamp(7, Timestamp.from(user.getCreatedAt()));
            psmt.setTimestamp(8, Timestamp.from(user.getUpdatedAt()));

            return psmt.executeUpdate();
        });
    }

    public User findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ? AND deleted_at IS NULL";

        return jdbcTemplate.execute(sql, psmt ->  {
            psmt.setString(1, email);

            try (ResultSet rs = psmt.executeQuery()) {
                if (rs.next()) {
                    return User.reconstituteUser(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("email"),
                        rs.getString("name"),
                        rs.getString("password"),
                        UserStatus.valueOf(rs.getString("status")),
                        UserRole.valueOf(rs.getString("role")),
                        toInstant(rs.getTimestamp("created_at")),
                        toInstant(rs.getTimestamp("updated_at")),
                        toInstant(rs.getTimestamp("deleted_at"))
                    );
                }
            }

            return null;
        });
    }

    private Instant toInstant(Timestamp timestamp) {
        return Optional.ofNullable(timestamp).map(Timestamp::toInstant).orElse(null);
    }

    public boolean existsUserByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ? AND deleted_at IS NULL LIMIT 1";

        return jdbcTemplate.execute(sql, psmt -> {
            psmt.setString(1, email);
            try (ResultSet rs = psmt.executeQuery()) {
                return rs.next();
            }
        });
    }
}
