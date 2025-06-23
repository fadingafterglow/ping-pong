package ua.edu.ukma.cs.game.objects;

import lombok.Getter;
import lombok.ToString;
import ua.edu.ukma.cs.enums.FieldCollisionOutcome;

@Getter
@ToString
public class Ball extends GameObject {

    private static final double EPS = 1e-6;
    private static final double SQRT_2 = Math.sqrt(2);

    private final int radius;
    private double speedX;
    private double speedY;

    public Ball(double x, double y, int radius, double speedX, double speedY) {
        super(x, y);
        this.radius = radius;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public void move() {
        this.x += speedX;
        this.y += speedY;
    }

    public FieldCollisionOutcome collideWith(Field field) {
        if (x - radius < field.getX())
            return FieldCollisionOutcome.LEFT_HIT;
        if (x + radius > field.getX() + field.getWidth())
            return FieldCollisionOutcome.RIGHT_HIT;
        if (y - radius < field.getY()) {
            y = field.getY() + radius;
            speedY = -speedY;
        }
        if (y + radius > field.getY() + field.getHeight()) {
            y = field.getY() + field.getHeight() - radius;
            speedY = -speedY;
        }
        return FieldCollisionOutcome.NONE;
    }

    public void collideWith(Racket racket) {
        double closestRacketX = Math.clamp(x, racket.getX(), racket.getX() + racket.getWidth());
        double closestRacketY = Math.clamp(y, racket.getY(), racket.getY() + racket.getHeight());

        double dx = x - closestRacketX;
        double dy = y - closestRacketY;

        if (dx * dx + dy * dy > radius * radius)
            return;

        double overlapX = radius - Math.abs(dx);
        double overlapY = radius - Math.abs(dy);

        if (Math.abs(overlapX - overlapY) < EPS) {
            speedX = -speedX;
            speedY = -speedY;
            x += dx > 0 ? overlapX / SQRT_2 : -overlapX / SQRT_2;
            y += dy > 0 ? overlapY / SQRT_2 : -overlapY / SQRT_2;
        } else if (overlapX < overlapY) {
            speedX = -speedX;
            x += dx > 0 ? overlapX : -overlapX;
        } else {
            speedY = -speedY;
            y += dy > 0 ? overlapY : -overlapY;
        }
    }
}
