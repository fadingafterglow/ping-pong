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
        setLayout(new GridBagLayout());
        var formPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        var loginButton = new JButton("Login");
        var toRegisterButton = new JButton("Register");
        formPanel.add(new JLabel("Username:", SwingConstants.CENTER));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:", SwingConstants.CENTER));
        formPanel.add(passwordField);
        var buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(loginButton);
        buttonPanel.add(toRegisterButton);
        var container = new JPanel(new BorderLayout());
        container.add(formPanel, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);
        add(container, new GridBagConstraints());
        loginButton.addActionListener(this::onLogin);
        toRegisterButton.addActionListener(e -> app.showRegister());
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
