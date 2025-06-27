package ua.edu.ukma.cs.repository;

import ua.edu.ukma.cs.entity.GameResultEntity;
import ua.edu.ukma.cs.filter.GameResultFilter;
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
            "thisUserScore", "(CASE WHEN creator_id = ? THEN creator_score ELSE other_score END)",
            "otherUserScore", "(CASE WHEN creator_id = ? THEN other_score ELSE creator_score END)",
            "timeFinished", "time_finished",
            "creatorId", "creator_id",
            "otherUserId", "other_user_id",
            "userId", "(creator_id = ? OR other_user_id = ?)"
    );

    public int create(GameResultEntity entity) {
        String sql = "INSERT INTO game_results (creator_score, other_score, time_finished, creator_id, other_user_id) VALUES (?, ?, ?, ?, ?)";
        return withStatementInCurrentTransaction(sql, true, statement -> {
            statement.setInt(1, entity.getCreatorScore());
            statement.setInt(2, entity.getOtherScore());
            statement.setTimestamp(3, TimeUtils.mapToSqlTimestamp(entity.getTimeFinished()));
            statement.setInt(4, entity.getCreatorId());
            statement.setInt(5, entity.getOtherUserId());

            statement.executeUpdate();

            return readGeneratedKey(statement);
        });
    }

    public Optional<GameResultEntity> getById(int id) {
        String sql = """
        SELECT r.id, r.creator_score, r.other_score, r.time_finished, r.creator_id, r.other_user_id,
               c.username AS creator_username, o.username AS other_username
        FROM game_results r
            JOIN users c ON r.creator_id = c.id
            JOIN users o ON r.other_user_id = o.id
        WHERE id = ?
        """;
        return withStatementInCurrentTransaction(sql, statement -> {
           statement.setInt(1, id);
           return queryOne(statement);
        });
    }

    public List<GameResultEntity> getAllByFilter(GameResultFilter filter) {
        String sql = """
        SELECT r.id, r.creator_score, r.other_score, r.time_finished, r.creator_id, r.other_user_id,
               c.username AS creator_username, o.username AS other_username
        FROM game_results r
            JOIN users c ON r.creator_id = c.id
            JOIN users o ON r.other_user_id = o.id
        """;
        sql = filter.addFilteringAndPagination(sql, FIELD_EXPRESSION_MAP);
        return withStatementInCurrentTransaction(sql, statement -> {
            filter.setParameters(statement);
            return queryAll(statement);
        });
    }

    public long countByFilter(GameResultFilter filter) {
        String sql = "SELECT COUNT(*) FROM game_results";
        sql = filter.addFiltering(sql, FIELD_EXPRESSION_MAP);
        return withStatementInCurrentTransaction(sql, statement -> {
            filter.setParameters(statement);
            return queryCount(statement);
        });
    }

    @Override
    protected GameResultEntity readEntityFromResultSet(ResultSet resultSet) throws SQLException {
        return GameResultEntity.builder()
                .id(resultSet.getInt("id"))
                .creatorScore(resultSet.getInt("creator_score"))
                .otherScore(resultSet.getInt("other_score"))
                .timeFinished(TimeUtils.mapToLocalDateTime(resultSet.getTimestamp("time_finished")))
                .creatorId(resultSet.getInt("creator_id"))
                .creatorUsername(resultSet.getString("creator_username"))
                .otherUserId(resultSet.getInt("other_user_id"))
                .otherUsername(resultSet.getString("other_username"))
                .build();
    }
}
