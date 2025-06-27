package ua.edu.ukma.cs.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameResultStats {
    private int totalGames;
    private int wins;
    private double averageScore;
}
