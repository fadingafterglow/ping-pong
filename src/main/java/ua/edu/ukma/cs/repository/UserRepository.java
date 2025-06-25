package ua.edu.ukma.cs.repository;

import ua.edu.ukma.cs.entity.UserEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

import ua.edu.ukma.cs.filter.UserExactEqualityFilter;
import ua.edu.ukma.cs.repository.base.BaseRepository;

public class UserRepository extends BaseRepository<UserEntity> {
    private static final Map<String, String> FIELD_EXPRESSION_MAP = Map.of(
            "id", "id",
            "username", "username"
    );

    public int create(UserEntity entity) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        return withStatementInCurrentTransaction(sql, true, statement -> {
            statement.setString(1, entity.getUsername());
            statement.setString(2, entity.getPasswordHash());

            statement.executeUpdate();

            return readGeneratedKey(statement);
        });
    }

    public Optional<UserEntity> findById(int id) {
        return findByExactFilter(UserExactEqualityFilter.builder().id(id).build());
    }

    public Optional<UserEntity> findByUsername(String username) {
        return findByExactFilter(UserExactEqualityFilter.builder().username(username).build());
    }

    private Optional<UserEntity> findByExactFilter(UserExactEqualityFilter filter) {
        String sql = "SELECT * FROM users";
        sql = filter.addFiltering(sql, FIELD_EXPRESSION_MAP);
        return withStatementInCurrentTransaction(sql, statement -> {
            filter.setParameters(statement);
            return queryOne(statement);
        });
    }

    @Override
    protected UserEntity readEntityFromResultSet(ResultSet resultSet) throws SQLException {
        return UserEntity.builder()
                .id(resultSet.getInt("id"))
                .username(resultSet.getString("username"))
                .passwordHash(resultSet.getString("password_hash"))
                .build();
    }
}
