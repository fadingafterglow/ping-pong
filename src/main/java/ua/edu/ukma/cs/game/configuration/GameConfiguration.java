package ua.edu.ukma.cs.game.configuration;

public record GameConfiguration
(
        int fieldWidth,
        int fieldHeight,
        int racketWidth,
        int racketHeight,
        int ballRadius,
        int initialBallSpeedX,
        int initialBallSpeedY
)
{}
