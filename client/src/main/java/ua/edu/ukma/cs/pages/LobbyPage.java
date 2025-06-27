package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.app.AppState;
import ua.edu.ukma.cs.app.PingPongClient;
import ua.edu.ukma.cs.connection.LobbyConnection;
import ua.edu.ukma.cs.game.lobby.GameLobbySnapshot;
import ua.edu.ukma.cs.game.lobby.GameLobbyState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class LobbyPage extends BasePage {

    private final JTextField lobbyIdLabel;
    private final JLabel lobbyMaxScoreLabel;
    private final JLabel lobbyStateLabel;

    private final JPanel playersPanel;

    private final JPanel buttonsPanel;
    private final JButton startGameButton;
    private final JButton leaveLobbyButton;

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

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(0, 1));
        startGameButton = new JButton("Start game");
        leaveLobbyButton = new JButton("Leave lobby");

        setLayout(new BorderLayout());
        add(centerPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    @Override
    public void init() {
        AppState appState = app.getAppState();
        LobbyConnection connection = appState.getLobbyConnection();
        GameLobbySnapshot snapshot = connection.getLobbyState();
        lobbyIdLabel.setText("Lobby id: " + appState.getLobbyId());
        updateAll(snapshot);
        buttonsPanel.removeAll();
        if (appState.getUserId() == snapshot.creatorId()) {
            buttonsPanel.add(startGameButton);
            startGameButton.addActionListener(e -> connection.sendStartGameRequest());
        }
        buttonsPanel.add(leaveLobbyButton);
        leaveLobbyButton.addActionListener(this::onLeaveLobby);
        connection.setOnLobbyUpdateCallback(this::onLobbyUpdate);
    }

    private void onLeaveLobby(ActionEvent e) {
        AppState appState = app.getAppState();
        LobbyConnection connection = appState.getLobbyConnection();
        if (connection != null) {
            connection.disconnect(false);
            app.getAppState().clearLobbyConnection();
        }
        app.showMainMenu();
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
