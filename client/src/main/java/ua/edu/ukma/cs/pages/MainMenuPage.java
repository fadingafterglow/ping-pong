package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.services.CreateLobbyService;
import ua.edu.ukma.cs.services.JoinLobbyService;
import ua.edu.ukma.cs.app.App;
import java.net.Socket;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class MainMenuPage extends BasePage {
    private final JButton createLobbyButton = new JButton("Create new lobby");
    private final JTextField uuidInput = new JTextField(30);
    private final JButton joinButton = new JButton("Join");
    private final App app;

    public MainMenuPage(App app, CreateLobbyService createLobbyService, JoinLobbyService joinLobbyService) {
        this.app = app;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(createLobbyButton, gbc);

        gbc.gridy++;
        add(new JLabel("Lobby UUID:"), gbc);
        gbc.gridy++;
        add(uuidInput, gbc);
        gbc.gridy++;
        add(joinButton, gbc);

        createLobbyButton.addActionListener(e -> {
            try {
                UUID lobbyId = createLobbyService.createLobby();
                Socket socket = joinLobbyService.joinLobby(lobbyId);
                app.showGame(socket);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(MainMenuPage.this, "Failed to create/join lobby: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        joinButton.addActionListener(e -> {
            String uuidText = uuidInput.getText().trim();
            if (uuidText.isEmpty()) {
                JOptionPane.showMessageDialog(MainMenuPage.this, "Please enter a lobby UUID.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            UUID lobbyId;
            try {
                lobbyId = UUID.fromString(uuidText);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(MainMenuPage.this, "Invalid UUID format.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                Socket socket = joinLobbyService.joinLobby(lobbyId);
                app.showGame(socket);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(MainMenuPage.this, "Failed to join lobby: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    @Override
    public void init() {
        uuidInput.setText("");
    }
} 