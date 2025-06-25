package ua.edu.ukma.cs.pages;

import javax.swing.*;
import java.awt.*;
import java.net.Socket;
import ua.edu.ukma.cs.tcp.packets.payload.JoinLobbyResponse;
import ua.edu.ukma.cs.game.lobby.GameLobbySnapshot;
import ua.edu.ukma.cs.game.configuration.GameConfiguration;
import ua.edu.ukma.cs.utils.SharedObjectMapper;
import java.io.InputStream;

public class GamePage extends BasePage {
    private Socket socket;

    public GamePage() {
        setLayout(new BorderLayout());
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void init() {
        try {
            InputStream in = socket.getInputStream();
            byte[] buffer = in.readNBytes(4096);
            JoinLobbyResponse response = SharedObjectMapper.S.readValue(buffer, JoinLobbyResponse.class);
            if (!response.isSuccess()) {
                add(new JLabel("Failed to join lobby: " + response.getMessage(), SwingConstants.CENTER), BorderLayout.CENTER);
                revalidate();
                repaint();
                return;
            }
            GameLobbySnapshot snapshot = response.getLobby();
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