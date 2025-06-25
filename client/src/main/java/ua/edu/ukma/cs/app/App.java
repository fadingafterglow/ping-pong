package ua.edu.ukma.cs.app;

import ua.edu.ukma.cs.pages.LoginPage;
import ua.edu.ukma.cs.pages.RegisterPage;
import ua.edu.ukma.cs.pages.CreateLobbyPage;
import ua.edu.ukma.cs.services.HttpService;
import ua.edu.ukma.cs.services.LoginService;
import ua.edu.ukma.cs.services.RegisterService;

import javax.swing.*;
import java.awt.*;

public class App extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private final HttpService httpService = new HttpService();
    private final RegisterService registerService = new RegisterService(httpService);
    private final LoginService loginService = new LoginService(httpService);

    private final RegisterPage registerPage;
    private final LoginPage loginPage;
    private final CreateLobbyPage createLobbyPage;

    public App() {
        setTitle("Game App");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        registerPage = new RegisterPage(this, registerService);
        loginPage = new LoginPage(this, loginService);
        createLobbyPage = new CreateLobbyPage();

        cards.add(registerPage, "register");
        cards.add(loginPage, "login");
        cards.add(createLobbyPage, "lobby");

        add(cards);
        showLogin();
    }

    public void showRegister() {
        registerPage.init();
        cardLayout.show(cards, "register");
    }

    public void showLogin() {
        loginPage.init();
        cardLayout.show(cards, "login");
    }

    public void showLobby() {
        createLobbyPage.init();
        cardLayout.show(cards, "lobby");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
