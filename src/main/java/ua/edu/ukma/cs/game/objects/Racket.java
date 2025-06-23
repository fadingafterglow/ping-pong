package ua.edu.ukma.cs.game.objects;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Racket extends GameObject {

    private final int width;
    private final int height;

    public Racket(int x, int y, int width, int height) {
        super(x, y);
        this.width = width;
        this.height = height;
    }

    public void move(int deltaY) {
        this.y += deltaY;
    }
}
