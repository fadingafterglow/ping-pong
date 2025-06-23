package ua.edu.ukma.cs.game.objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class Ball extends GameObject {

    private final int radius;
    private double speedX;
    private double speedY;

    public Ball(int x, int y, int radius, double speedX, double speedY) {
        super(x, y);
        this.radius = radius;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public void bounce(double collisionAngle) {
        double speed = Math.sqrt(speedX * speedX + speedY * speedY);
        speedX = speed * Math.cos(collisionAngle);
        speedY = speed * Math.sin(collisionAngle);
    }
}
