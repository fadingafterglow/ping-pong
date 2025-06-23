package ua.edu.ukma.cs.game.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public abstract class GameObject {

    protected int x;
    protected int y;
}
