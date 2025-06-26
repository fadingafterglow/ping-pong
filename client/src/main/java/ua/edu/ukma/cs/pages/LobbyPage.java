package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.app.PingPongClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class LobbyPage extends BasePage {

    private final PingPongClient app;

    private final JLabel lobbyIdLabel;
    private final JLabel lobbyMaxScoreLabel;
    private final JLabel lobbyStateLabel;

    private final JPanel playersPanel;

    public LobbyPage(PingPongClient app) {
        this.app = app;

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK),
                        "Lobby Info"
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        lobbyIdLabel = new JLabel("?id", SwingConstants.LEFT);
        lobbyMaxScoreLabel = new JLabel("?maxScore", SwingConstants.LEFT);
        lobbyStateLabel = new JLabel("?state", SwingConstants.LEFT);
        leftPanel.add(lobbyIdLabel);
        leftPanel.add(lobbyMaxScoreLabel);

        playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        playersPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK),
                        "Players"
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 0, 10));
        centerPanel.add(leftPanel);
        centerPanel.add(playersPanel);

        JPanel bottomPanel = new JPanel();
        JButton startGameButton = new JButton("Start game");
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int halfWidth = getWidth() / 2;
                startGameButton.setPreferredSize(new Dimension(halfWidth, 30));
                bottomPanel.revalidate();
            }
        });
        bottomPanel.add(startGameButton);

        setLayout(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
