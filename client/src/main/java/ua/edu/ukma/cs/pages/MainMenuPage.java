package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.game.lobby.GameLobbyState;
import ua.edu.ukma.cs.services.CreateLobbyService;
import ua.edu.ukma.cs.services.JoinLobbyService;
import ua.edu.ukma.cs.app.PingPongClient;
import ua.edu.ukma.cs.connection.LobbyConnection;
import ua.edu.ukma.cs.utils.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.UUID;

public class MainMenuPage extends BasePage {

    private final CreateLobbyService createLobbyService;
    private final JoinLobbyService joinLobbyService;

    public MainMenuPage(PingPongClient app, CreateLobbyService createLobbyService, JoinLobbyService joinLobbyService) {
        super(app);
        this.createLobbyService = createLobbyService;
        this.joinLobbyService = joinLobbyService;

        var buttonPanel = new JPanel(new GridLayout(5, 1, 0, 5));
        JButton createLobbyButton = new JButton("Create new lobby");
        createLobbyButton.addActionListener(this::onCreateLobby);
        JButton joinLobbyButton = new JButton("Join existing lobby");
        joinLobbyButton.addActionListener(this::onJoinLobby);
        JButton gamesResultsButton = new JButton("View games results");
        gamesResultsButton.addActionListener(e -> app.showGamesResults());
        JButton profileButton = new JButton("View profile");
        profileButton.addActionListener(e -> app.showProfile());
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> app.exit());
        buttonPanel.add(createLobbyButton);
        buttonPanel.add(joinLobbyButton);
        buttonPanel.add(gamesResultsButton);
        buttonPanel.add(profileButton);
        buttonPanel.add(exitButton);

        setLayout(new GridBagLayout());
        add(buttonPanel);
    }

    private void onCreateLobby(ActionEvent e) {
        try {
            UUID lobbyId = createLobbyService.createLobby();
            setupConnection(lobbyId);
            app.showLobby();
        } catch (Exception ex) {
            DialogUtils.errorDialog(this, "Failed to create lobby.");
        }
    }

    private void onJoinLobby(ActionEvent e) {
        try {
            String input = DialogUtils.inputDialog(this, "Enter lobby id:");
            if (input == null) return;
            UUID lobbyId = UUID.fromString(input);
            LobbyConnection connection = setupConnection(lobbyId);
            if (connection.getLobbyState().state() == GameLobbyState.IN_PROGRESS)
                app.showGame();
            else
                app.showLobby();
        } catch (IllegalArgumentException ex) {
            DialogUtils.errorDialog(this, "Invalid id format.");
        } catch (Exception ex) {
            DialogUtils.errorDialog(this, "Failed to join lobby.");
        }
    }

    private LobbyConnection setupConnection(UUID lobbyId) {
        LobbyConnection connection = joinLobbyService.joinLobby(lobbyId, app.getAppState().getJwtToken());
        connection.setOnDisconnectCallback(this::onDisconnect);
        app.getAppState().setLobbyConnection(lobbyId, connection);
        return connection;
    }

    private void onDisconnect() {
        app.getAppState().clearLobbyConnection();
        DialogUtils.errorDialog(app, "Connection lost.");
        app.showMainMenu();
    }
} 