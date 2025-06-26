package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.app.App;
import ua.edu.ukma.cs.api.request.RegisterUserRequestDto;
import ua.edu.ukma.cs.services.RegisterService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RegisterPage extends BasePage {

    private final App app;
    private final JTextField usernameField;

    private final JPasswordField passwordField;
    private final RegisterService registerService;

    public RegisterPage(App app, RegisterService registerService) {
        this.app = app;
        this.registerService = registerService;

        var formPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        formPanel.add(new JLabel("Username:", SwingConstants.CENTER));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:", SwingConstants.CENTER));
        formPanel.add(passwordField);

        var buttonPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        var registerButton = new JButton("Register");
        registerButton.addActionListener(this::onRegister);
        var toLoginButton = new JButton("Go to login page");
        toLoginButton.addActionListener(e -> app.showLogin());
        buttonPanel.add(registerButton);
        buttonPanel.add(toLoginButton);

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

    private void onRegister(ActionEvent e) {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            registerService.register(
                    RegisterUserRequestDto.builder()
                            .username(username)
                            .password(password)
                            .build()
            );

            JOptionPane.showMessageDialog(this, "Registration successful! Please log in.");
            app.showLogin();
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
