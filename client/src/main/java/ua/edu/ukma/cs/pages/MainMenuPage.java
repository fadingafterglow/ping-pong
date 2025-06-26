package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.app.AppState;
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

    private final PingPongClient app;
    private final CreateLobbyService createLobbyService;
    private final JoinLobbyService joinLobbyService;

    public MainMenuPage(PingPongClient app, CreateLobbyService createLobbyService, JoinLobbyService joinLobbyService) {
        this.app = app;
        this.createLobbyService = createLobbyService;
        this.joinLobbyService = joinLobbyService;

        var buttonPanel = new JPanel(new GridLayout(3, 1, 0, 5));
        JButton createLobbyButton = new JButton("Create new lobby");
        createLobbyButton.addActionListener(this::onCreateLobby);
        JButton joinLobbyButton = new JButton("Join existing lobby");
        joinLobbyButton.addActionListener(this::onJoinLobby);
        JButton viewGamesButton = new JButton("View game history");
        buttonPanel.add(createLobbyButton);
        buttonPanel.add(joinLobbyButton);
        buttonPanel.add(viewGamesButton);

        setLayout(new GridBagLayout());
        add(buttonPanel);
    }

    private void onCreateLobby(ActionEvent e) {
        try {
            UUID lobbyId = createLobbyService.createLobby();
            LobbyConnection connection = joinLobbyService.joinLobby(lobbyId, app.getAppState().getJwtToken());
            app.getAppState().setLobbyConnection(lobbyId, connection);
            app.showLobby();
        } catch (Exception ex) {
            DialogUtils.errorDialog(this, "Failed to create lobby.");
        }
    }

    private void onJoinLobby(ActionEvent e) {
        try {
            String input = DialogUtils.inputDialog(this, "Enter lobby id:");
            UUID lobbyId = UUID.fromString(input);
            LobbyConnection connection = joinLobbyService.joinLobby(lobbyId, app.getAppState().getJwtToken());
            app.getAppState().setLobbyConnection(lobbyId, connection);
            app.showLobby();
        } catch (IllegalArgumentException ex) {
            DialogUtils.errorDialog(this, "Invalid id format.");
        } catch (Exception ex) {
            DialogUtils.errorDialog(this, "Failed to join lobby.");
        }
    }
} 