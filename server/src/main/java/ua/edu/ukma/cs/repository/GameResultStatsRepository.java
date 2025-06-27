package ua.edu.ukma.cs.repository;

import ua.edu.ukma.cs.entity.GameResultStats;
import ua.edu.ukma.cs.repository.base.BaseRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class GameResultStatsRepository extends BaseRepository<GameResultStats> {
    public Optional<GameResultStats> getStatsForUser(int userId) {
        String sql = """
                WITH all_games_of_user AS (
                    SELECT (CASE WHEN creator_id = ? THEN creator_score ELSE other_score END) AS this_user_score,
                           (CASE WHEN creator_id = ? THEN other_score ELSE creator_score END) AS other_user_score,
                    FROM game_results
                    WHERE creator_id = ? OR other_user_id = ?
                )
                SELECT COUNT(*) AS total_games,
                       COUNT FILTER (WHERE this_user_score > other_user_score) AS wins,
                       AVG(this_user_score) AS average_score
                FROM all_games_of_user;
                """;

        return withStatementInCurrentTransaction(sql, statement -> {
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            statement.setInt(3, userId);
            statement.setInt(4, userId);

            return queryOne(statement);
        });
    }

    @Override
    protected GameResultStats readEntityFromResultSet(ResultSet resultSet) throws SQLException {
        return GameResultStats.builder()
                .totalGames(resultSet.getInt("total_games"))
                .wins(resultSet.getInt("wins"))
                .averageScore(resultSet.getDouble("average_score"))
                .build();
    }
}
