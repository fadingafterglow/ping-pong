package ua.edu.ukma.cs.game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.atomic.AtomicBoolean;

public class ArrowKeysListener implements KeyListener {

    private AtomicBoolean isUpPressed;
    private AtomicBoolean isDownPressed;

    public ArrowKeysListener() {
        this.isUpPressed = new AtomicBoolean(false);
        this.isDownPressed = new AtomicBoolean(false);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            isUpPressed.set(true);
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            isDownPressed.set(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            isUpPressed.set(false);
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            isDownPressed.set(false);
        }
    }

    public boolean isUpPressed() {
        return isUpPressed.get();
    }
    public boolean isDownPressed() {
        return isDownPressed.get();
    }
}
