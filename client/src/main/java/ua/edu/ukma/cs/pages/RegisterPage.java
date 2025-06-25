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
        setLayout(new GridBagLayout());
        var formPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        var registerButton = new JButton("Register");
        formPanel.add(new JLabel("Username:", SwingConstants.CENTER));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:", SwingConstants.CENTER));
        formPanel.add(passwordField);
        var buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(registerButton);
        var container = new JPanel(new BorderLayout());
        container.add(formPanel, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);
        add(container, new GridBagConstraints());
        registerButton.addActionListener(this::onRegister);
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
