package ua.edu.ukma.cs.game;

import ua.edu.ukma.cs.game.configuration.GameConfiguration;
import ua.edu.ukma.cs.game.state.GameState;

import javax.swing.*;

public class GameTest {

    public static void main(String[] args) throws Exception {
        GameConfiguration config = GameConfiguration.builder()
                .fieldWidth(800)
                .fieldHeight(600)
                .racketWidth(10)
                .racketHeight(100)
                .racketSpeed(6)
                .ballRadius(8)
                .initialBallSpeed(5)
                .build();
        GameState gameState = new GameState(config);

        ArrowKeysListener arrowKeysListener = new ArrowKeysListener();

        GamePanel panel = new GamePanel(config);
        JFrame frame = new JFrame("Pong UI");
        frame.addKeyListener(arrowKeysListener);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);

        int i = 0;
        while (true) {
            gameState.update();
            if (arrowKeysListener.isUpPressed())
                gameState.moveRacket(false, true);
            if (arrowKeysListener.isDownPressed())
                gameState.moveRacket(true, true);
            gameState.moveRacket(i++ % 400 < 200, false);
            panel.updateState(gameState.takeSnapshot());
            Thread.sleep(10);
        }
    }
}
