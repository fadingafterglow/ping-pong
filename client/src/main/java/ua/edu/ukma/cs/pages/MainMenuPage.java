package ua.edu.ukma.cs.pages;

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

        var buttonPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        JButton createLobbyButton = new JButton("Create new lobby");
        createLobbyButton.addActionListener(this::onCreateLobby);
        JButton joinLobbyButton = new JButton("Join existing lobby");
        joinLobbyButton.addActionListener(this::onJoinLobby);
        JButton viewGamesButton = new JButton("View game history");
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> app.exit());
        buttonPanel.add(createLobbyButton);
        buttonPanel.add(joinLobbyButton);
        buttonPanel.add(viewGamesButton);
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
            setupConnection(lobbyId);
            app.showLobby();
        } catch (IllegalArgumentException ex) {
            DialogUtils.errorDialog(this, "Invalid id format.");
        } catch (Exception ex) {
            DialogUtils.errorDialog(this, "Failed to join lobby.");
        }
    }

    private void setupConnection(UUID lobbyId) {
        LobbyConnection connection = joinLobbyService.joinLobby(lobbyId, app.getAppState().getJwtToken());
        connection.setOnDisconnectCallback(this::onDisconnect);
        app.getAppState().setLobbyConnection(lobbyId, connection);
    }

    private void onDisconnect() {
        app.getAppState().clearLobbyConnection();
        DialogUtils.errorDialog(this, "Connection lost.");
        app.showMainMenu();
    }
} 