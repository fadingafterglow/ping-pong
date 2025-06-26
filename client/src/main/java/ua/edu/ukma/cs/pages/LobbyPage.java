package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.app.AppState;
import ua.edu.ukma.cs.app.PingPongClient;
import ua.edu.ukma.cs.connection.LobbyConnection;
import ua.edu.ukma.cs.game.lobby.GameLobbySnapshot;
import ua.edu.ukma.cs.game.lobby.GameLobbyState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class LobbyPage extends BasePage {

    private final JTextField lobbyIdLabel;
    private final JLabel lobbyMaxScoreLabel;
    private final JLabel lobbyStateLabel;

    private final JPanel playersPanel;

    private final JButton startGameButton;

    public LobbyPage(PingPongClient app) {
        super(app);

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK),
                        "Lobby Info"
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        lobbyMaxScoreLabel = new JLabel("?maxScore", SwingConstants.LEFT);
        lobbyStateLabel = new JLabel("?state", SwingConstants.LEFT);
        lobbyIdLabel = new JTextField();
        lobbyIdLabel.setEditable(false);
        lobbyIdLabel.setBackground(null);
        lobbyIdLabel.setBorder(null);
        lobbyIdLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, lobbyMaxScoreLabel.getPreferredSize().height));
        leftPanel.add(lobbyIdLabel);
        leftPanel.add(lobbyMaxScoreLabel);
        leftPanel.add(lobbyStateLabel);

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
        startGameButton = new JButton("Start game");
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

    @Override
    public void init() {
        AppState appState = app.getAppState();
        LobbyConnection connection = appState.getLobbyConnection();
        GameLobbySnapshot snapshot = connection.getLobbyState();
        lobbyIdLabel.setText("Lobby id: " + appState.getLobbyId());
        updateAll(snapshot);
        if (appState.getUserId() != snapshot.creatorId())
            startGameButton.setVisible(false);
        connection.setOnLobbyUpdateCallback(this::onLobbyUpdate);
        startGameButton.addActionListener(e -> connection.sendStartGameRequest());
    }

    private void onLobbyUpdate(LobbyConnection connection) {
        GameLobbySnapshot snapshot = connection.getLobbyState();
        if (snapshot.state() == GameLobbyState.IN_PROGRESS) {
            connection.setOnLobbyUpdateCallback(null);
            app.showGame();
            return;
        }
        updateAll(snapshot);
    }

    private void updateAll(GameLobbySnapshot snapshot) {
        updateLobbyInfo(snapshot);
        updatePlayers(snapshot);
        updateStartGameButton(snapshot);
    }

    private void updateLobbyInfo(GameLobbySnapshot snapshot) {
        lobbyMaxScoreLabel.setText("Max score: " + snapshot.gameConfiguration().maxScore());
        lobbyStateLabel.setText("State: " + getLobbyStateText(snapshot));
    }

    private void updatePlayers(GameLobbySnapshot snapshot) {
        playersPanel.removeAll();
        if (snapshot.creatorUsername() != null)
            playersPanel.add(new JLabel("Player 1: " + snapshot.creatorUsername()));
        if (snapshot.otherPlayerUsername() != null)
            playersPanel.add(new JLabel("Player 2: " + snapshot.otherPlayerUsername()));
        playersPanel.revalidate();
        playersPanel.repaint();
    }

    private void updateStartGameButton(GameLobbySnapshot snapshot) {
        startGameButton.setEnabled(canStartGame(snapshot));
    }

    private String getLobbyStateText(GameLobbySnapshot snapshot) {
        if (canStartGame(snapshot))
            return "Ready to start";
        return switch (snapshot.state()) {
            case WAITING -> "Waiting for players";
            case IN_PROGRESS -> "Game is in progress";
            case FINISHED -> "Game finished";
        };
    }

    private boolean canStartGame(GameLobbySnapshot snapshot) {
        return snapshot.state() == GameLobbyState.WAITING
                && snapshot.creatorUsername() != null
                && snapshot.otherPlayerUsername() != null;
    }
}
