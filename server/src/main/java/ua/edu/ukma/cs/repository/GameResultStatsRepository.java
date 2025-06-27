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
                    SELECT r.creator_id, r.other_user_id, r.creator_score, r.other_user_score,
                           CASE
                                WHEN r.creator_id = ? THEN r.creator_score
                                ELSE r.other_score
                           END AS user_score
                    FROM game_results r
                        JOIN users c ON r.creator_id = c.id
                        JOIN users o ON r.other_user_id = o.id
                    WHERE c.id = ? OR o.id = ?
                )
                SELECT COUNT(*) AS total_games,
                       COUNT FILTER (WHERE user_score > LEAST(creator_score, other_score)) AS wins,
                       AVG(user_score) AS average_score
                FROM all_games_of_user;
                """;

        return withStatementInCurrentTransaction(sql, statement -> {
            statement.setInt(1, userId);
            statement.setInt(2, userId);
            statement.setInt(3, userId);

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
