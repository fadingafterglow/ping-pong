package ua.edu.ukma.cs.game.configuration;

import lombok.Builder;

@Builder
public record GameConfiguration
(
        int fieldWidth,
        int fieldHeight,
        int racketWidth,
        int racketHeight,
        int racketSpeed,
        int ballRadius,
        int initialBallSpeed,
        int maxScore
)
{}
