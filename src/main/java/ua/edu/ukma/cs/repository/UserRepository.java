package ua.edu.ukma.cs.repository;

import ua.edu.ukma.cs.entity.UserEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import ua.edu.ukma.cs.exception.DataBaseException;

public class UserRepository extends BaseRepository {
    public int create(UserEntity entity) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        return withStatementInCurrentTransaction(sql, true, statement -> {
            statement.setString(1, entity.getUsername());
            statement.setString(2, entity.getPasswordHash());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next())
                    return generatedKeys.getInt(1);
                else
                    throw new DataBaseException("Cannot create user");
            }
        });
    }

    public Optional<UserEntity> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return withStatementInCurrentTransaction(sql, false, statement -> {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        });
    }

    public Optional<UserEntity> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return withStatementInCurrentTransaction(sql, false, statement -> {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        });
    }

    private UserEntity map(ResultSet resultSet) throws SQLException {
        return UserEntity.builder()
                .id(resultSet.getInt("id"))
                .username(resultSet.getString("username"))
                .passwordHash(resultSet.getString("password_hash"))
                .build();
    }
}
