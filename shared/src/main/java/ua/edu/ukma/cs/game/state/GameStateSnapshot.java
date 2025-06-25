package ua.edu.ukma.cs.game.state;

import lombok.Builder;

@Builder
public record GameStateSnapshot
(
    int player1Score,
    int player2Score,
    int player1RacketX,
    int player1RacketY,
    int player2RacketX,
    int player2RacketY,
    int ballX,
    int ballY
)
{}
