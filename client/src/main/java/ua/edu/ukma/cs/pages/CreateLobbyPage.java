package ua.edu.ukma.cs.pages;

import javax.swing.*;
import java.awt.*;

public class CreateLobbyPage extends BasePage {
    private final JButton createLobbyButton = new JButton("Create new lobby");

    public CreateLobbyPage() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(createLobbyButton, gbc);
    }

    @Override
    public void init() {
        
    }

    public JButton getCreateLobbyButton() {
        return createLobbyButton;
    }
} 