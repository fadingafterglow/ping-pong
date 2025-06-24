package ua.edu.ukma.cs.repository;

import ua.edu.ukma.cs.entity.GameResultEntity;
import ua.edu.ukma.cs.repository.base.BaseRepository;
import ua.edu.ukma.cs.utils.TimeUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GameResultRepository extends BaseRepository<GameResultEntity> {
    private static final Map<String, String> FIELD_EXPRESSION_MAP = Map.of(
            "id", "id",
            "score", "score",
            "timeFinished", "time_finished",
            "creatorId", "creator_id",
            "otherUserId", "other_user_id"
    );

    public int create(GameResultEntity entity) {
        String sql = "INSERT INTO game_results (score, time_finished, creator_id, other_user_id) VALUES (?, ?, ?, ?)";
        return withStatementInCurrentTransaction(sql, true, statement -> {
            statement.setInt(1, entity.getScore());
            statement.setTimestamp(2, TimeUtils.mapToSqlTimestamp(entity.getTimeFinished()));
            statement.setInt(3, entity.getCreatorId());
            statement.setInt(4, entity.getOtherUserId());

            statement.executeUpdate();

            return readGeneratedKey(statement);
        });
    }

    public Optional<GameResultEntity> getById(int id) {
        String sql = "SELECT FROM game_results WHERE id = ?";
        return withStatementInCurrentTransaction(sql, false, statement -> {
           statement.setInt(1, id);
           return queryOne(statement);
        });
    }

    public List<GameResultEntity> getAllUserGameResults(int userId) {
        String sql = "SELECT FROM game_results WHERE creator_id = ? OR other_user_id = ?";
        return withStatementInCurrentTransaction(sql, false, statement -> {
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            return queryAll(statement);
        });
    }

    @Override
    protected GameResultEntity readEntityFromResultSet(ResultSet resultSet) throws SQLException {
        return GameResultEntity.builder()
                .id(resultSet.getInt("id"))
                .score(resultSet.getInt("score"))
                .timeFinished(TimeUtils.mapToLocalDateTime(resultSet.getTimestamp("time_finished")))
                .creatorId(resultSet.getInt("creator_id"))
                .otherUserId(resultSet.getInt("other_user_id"))
                .build();
    }
}
