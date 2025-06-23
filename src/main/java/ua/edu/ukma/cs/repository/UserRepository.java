package ua.edu.ukma.cs.repository;

import ua.edu.ukma.cs.entity.UserEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import ua.edu.ukma.cs.exception.DataBaseException;

public class UserRepository extends BaseRepository {
    public int createUser(UserEntity entity) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (PreparedStatement statement = transactionManager.currentTransaction().prepareStatement(sql, true)) {
            statement.setString(1, entity.getUsername());
            statement.setString(2, entity.getPasswordHash());
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next())
                    return generatedKeys.getInt(1);
                else
                    throw new DataBaseException("Cannot create user");
            }
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    public Optional<UserEntity> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement statement = transactionManager.currentTransaction().prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    public Optional<UserEntity> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement statement = transactionManager.currentTransaction().prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(map(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataBaseException(e);
        }
    }

    private UserEntity map(ResultSet resultSet) throws SQLException {
        return UserEntity.builder()
                .id(resultSet.getInt("id"))
                .username(resultSet.getString("username"))
                .passwordHash(resultSet.getString("password_hash"))
                .build();
    }
}
