package ua.edu.ukma.cs.game;

import ua.edu.ukma.cs.game.configuration.GameConfiguration;
import ua.edu.ukma.cs.game.state.GameStateSnapshot;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    private final GameConfiguration config;
    private GameStateSnapshot state;

    public GamePanel(GameConfiguration config) {
        this.config = config;
        setPreferredSize(new Dimension(config.fieldWidth(), config.fieldHeight()));
        setBackground(Color.BLACK);
    }

    public void updateState(GameStateSnapshot state) {
        this.state = state;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.WHITE);

        g2.fillRect(state.player1RacketX(), state.player1RacketY(),
                config.racketWidth(), config.racketHeight());

        g2.fillRect(state.player2RacketX(), state.player2RacketY(),
                config.racketWidth(), config.racketHeight());

        int d = config.ballRadius() * 2;
        g2.fillOval(state.ballX() - config.ballRadius(),
                state.ballY() - config.ballRadius(), d, d);

        g2.setFont(new Font("Monospaced", Font.BOLD, 20));
        String scoreText = state.player1Score() + " : " + state.player2Score();
        g2.drawString(scoreText, config.fieldWidth() / 2 - 30, 30);
    }
}

