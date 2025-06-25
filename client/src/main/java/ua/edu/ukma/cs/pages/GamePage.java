package ua.edu.ukma.cs.pages;

import javax.swing.*;
import java.awt.*;

import ua.edu.ukma.cs.connection.LobbyConnection;
import ua.edu.ukma.cs.game.lobby.GameLobbySnapshot;
import ua.edu.ukma.cs.game.configuration.GameConfiguration;

public class GamePage extends BasePage {
    private LobbyConnection connection;

    public GamePage() {
        setLayout(new BorderLayout());
    }

    public void setConnection(LobbyConnection connection) {
        this.connection = connection;
    }

    @Override
    public void init() {
        try {
            GameLobbySnapshot snapshot = connection.getLobbyState();
            GameConfiguration config = snapshot.gameConfiguration();
            JPanel field = new JPanel(null);
            field.setPreferredSize(new Dimension(config.fieldWidth(), config.fieldHeight()));
            JLabel racket1 = new JLabel();
            racket1.setBackground(Color.BLUE);
            racket1.setOpaque(true);
            racket1.setBounds(10, config.fieldHeight()/2 - config.racketHeight()/2, config.racketWidth(), config.racketHeight());
            JLabel racket2 = new JLabel();
            racket2.setBackground(Color.RED);
            racket2.setOpaque(true);
            racket2.setBounds(config.fieldWidth()-10-config.racketWidth(), config.fieldHeight()/2 - config.racketHeight()/2, config.racketWidth(), config.racketHeight());
            field.add(racket1);
            field.add(racket2);
            add(field, BorderLayout.CENTER);
        } catch (Exception ex) {
            add(new JLabel("Error: " + ex.getMessage(), SwingConstants.CENTER), BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }
} 