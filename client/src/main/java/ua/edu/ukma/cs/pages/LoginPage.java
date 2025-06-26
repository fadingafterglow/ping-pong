package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.app.App;
import ua.edu.ukma.cs.app.AppState;
import ua.edu.ukma.cs.api.request.LoginUserRequestDto;
import ua.edu.ukma.cs.services.LoginService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginPage extends BasePage {

    private final App app;
    private final LoginService loginService;

    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public LoginPage(App app, LoginService loginService) {
        this.app = app;
        this.loginService = loginService;

        var formPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        formPanel.add(new JLabel("Username:", SwingConstants.CENTER));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:", SwingConstants.CENTER));
        formPanel.add(passwordField);

        var buttonPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        var loginButton = new JButton("Login");
        loginButton.addActionListener(this::onLogin);
        var toRegisterButton = new JButton("Go to registration page");
        toRegisterButton.addActionListener(e -> app.showRegister());
        buttonPanel.add(loginButton);
        buttonPanel.add(toRegisterButton);

        var mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(buttonPanel);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        add(mainPanel, gbc);
    }

    private void onLogin(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            String token = loginService.login(
                    LoginUserRequestDto.builder()
                        .username(username)
                        .password(password)
                        .build()
            );

            AppState.setJwtToken(token);
            JOptionPane.showMessageDialog(this, "Login successful.");
            app.showLobby();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void init() {
        usernameField.setText("");
        passwordField.setText("");
    }
}
