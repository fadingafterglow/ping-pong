package ua.edu.ukma.cs.game;

import lombok.Getter;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

@Getter
public class ArrowKeysListener implements KeyListener {

    private volatile boolean isUpPressed;
    private volatile boolean isDownPressed;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            isUpPressed = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            isDownPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            isUpPressed = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            isDownPressed = false;
        }
    }
}
