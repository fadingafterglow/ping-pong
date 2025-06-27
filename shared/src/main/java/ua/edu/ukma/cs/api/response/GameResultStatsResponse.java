package ua.edu.ukma.cs.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameResultStatsResponse {
    private int totalGames;
    private int wins;
    private double averageScore;
}