package ua.edu.ukma.cs.game.state;

import lombok.Getter;
import ua.edu.ukma.cs.game.objects.FieldCollisionOutcome;
import ua.edu.ukma.cs.game.configuration.GameConfiguration;
import ua.edu.ukma.cs.game.objects.Ball;
import ua.edu.ukma.cs.game.objects.Field;
import ua.edu.ukma.cs.game.objects.Racket;

import java.util.Random;

public class GameState {

    @Getter
    private final GameConfiguration config;
    private final Random rand;

    private int player1Score;
    private int player2Score;

    private final Field field;
    private final Racket player1Racket;
    private final Racket player2Racket;
    private Ball ball;

    public GameState(GameConfiguration config) {
        this.rand = new Random();
        this.config = config;
        this.field = createField();
        this.player1Racket = createRacket(true);
        this.player2Racket = createRacket(false);
        this.ball = createBall();
    }

    private Field createField() {
        return new Field(config.fieldWidth(), config.fieldHeight());
    }

    private Ball createBall() {
        double horizontalSpeedCoefficient = Math.clamp(rand.nextDouble(), 0.2, 0.8);
        double verticalSpeedCoefficient = Math.sqrt(1 - horizontalSpeedCoefficient * horizontalSpeedCoefficient);
        return new Ball(
                config.fieldWidth() / 2.0,
                config.fieldHeight() / 2.0,
                config.ballRadius(),
                config.initialBallSpeed() * horizontalSpeedCoefficient * (rand.nextBoolean() ? 1 : -1),
                config.initialBallSpeed() * verticalSpeedCoefficient * (rand.nextBoolean() ? 1 : -1)
        );
    }

    private Racket createRacket(boolean left) {
        int racketX = left ?
                2 * config.ballRadius() :
                field.getWidth() - config.racketWidth() - 2 * config.ballRadius();
        int racketY = (field.getHeight() - config.racketHeight()) / 2;
        return new Racket(racketX, racketY, config.racketWidth(), config.racketHeight());
    }

    public boolean update() {
        ball.move();
        FieldCollisionOutcome fieldCollision = ball.collideWith(field);
        if (fieldCollision == FieldCollisionOutcome.LEFT_HIT) {
            ball = createBall();
            return ++player2Score < config.maxScore();
        } else if (fieldCollision == FieldCollisionOutcome.RIGHT_HIT) {
            ball = createBall();
            return ++player1Score < config.maxScore();
        }
        ball.collideWith(player1Racket);
        ball.collideWith(player2Racket);
        return true;
    }

    public void moveRacket(boolean up, boolean isPlayer1) {
        Racket racket = isPlayer1 ? player1Racket : player2Racket;
        racket.move(up ? config.racketSpeed() : -config.racketSpeed(), field.getY(), field.getY() + field.getHeight());
    }

    public GameStateSnapshot takeSnapshot() {
        return GameStateSnapshot.builder()
                .player1Score(player1Score)
                .player2Score(player2Score)
                .player1RacketX((int) player1Racket.getX())
                .player1RacketY((int) player1Racket.getY())
                .player2RacketX((int) player2Racket.getX())
                .player2RacketY((int) player2Racket.getY())
                .ballX((int) ball.getX())
                .ballY((int) ball.getY())
                .build();
    }
}
