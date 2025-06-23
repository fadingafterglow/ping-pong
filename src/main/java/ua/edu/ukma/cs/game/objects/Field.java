package ua.edu.ukma.cs.game.objects;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Field extends GameObject {

    private final int width;
    private final int height;

    public Field(int width, int height) {
        super(0, 0);
        this.width = width;
        this.height = height;
    }
}
