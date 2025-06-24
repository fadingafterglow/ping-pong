package ua.edu.ukma.cs.repository;

import ua.edu.ukma.cs.entity.GameResultEntity;
import ua.edu.ukma.cs.repository.base.BaseRepository;
import ua.edu.ukma.cs.utils.TimeUtils;

import java.util.Map;

public class GameResultRepository extends BaseRepository {
    private static final Map<String, String> FIELD_EXPRESSION_MAP = Map.of(
            "id", "id",
            "score", "score",
            "timeFinished", "time_finished",
            "creatorId", "creator_id",
            "otherUserId", "other_user_id"
    );

    public int create(GameResultEntity entity) {
        String sql = "INSERT INTO game_results (score, timeFinished, creatorId, otherUserId) VALUES (?, ?, ?, ?)";
        return withStatementInCurrentTransaction(sql, true, statement -> {
            statement.setInt(1, entity.getScore());
            statement.setTimestamp(2, TimeUtils.mapToSqlTimestamp(entity.getTimeFinished()));
            statement.setInt(3, entity.getCreatorId());
            statement.setInt(4, entity.getOtherUserId());

            statement.executeUpdate();

            return readGeneratedKey(statement);
        });
    }
}
