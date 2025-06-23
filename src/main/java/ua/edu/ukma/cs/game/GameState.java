package ua.edu.ukma.cs.game;

import ua.edu.ukma.cs.game.configuration.GameConfiguration;
import ua.edu.ukma.cs.game.objects.Ball;
import ua.edu.ukma.cs.game.objects.Field;
import ua.edu.ukma.cs.game.objects.Racket;

public class GameState {

    private int player1Score;
    private int player2Score;

    private Field field;
    private Ball ball;
    private Racket player1Racket;
    private Racket player2Racket;

    public GameState(GameConfiguration config) {
        this.field = createField(config);
        this.ball = createBall(config);
        this.player1Racket = createRacket(config, true);
        this.player2Racket = createRacket(config, false);
    }

    private Field createField(GameConfiguration config) {
        return new Field(config.fieldWidth(), config.fieldHeight());
    }

    private Ball createBall(GameConfiguration config) {
        return new Ball(
                config.fieldWidth() / 2,
                config.fieldHeight() / 2,
                config.ballRadius(),
                config.initialBallSpeedX(),
                config.initialBallSpeedY()
        );
    }

    private Racket createRacket(GameConfiguration config, boolean left) {
        int racketX = left ?
                2 * config.ballRadius() :
                field.getWidth() - config.racketWidth() - 2 * config.ballRadius();
        int racketY = (field.getHeight() - config.racketHeight()) / 2;
        return new Racket(racketX, racketY, config.racketWidth(), config.racketHeight());
    }
}
