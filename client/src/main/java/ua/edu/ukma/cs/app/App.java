package ua.edu.ukma.cs.app;

import ua.edu.ukma.cs.connection.LobbyConnection;
import ua.edu.ukma.cs.pages.GamePage;
import ua.edu.ukma.cs.pages.LoginPage;
import ua.edu.ukma.cs.pages.RegisterPage;
import ua.edu.ukma.cs.pages.MainMenuPage;
import ua.edu.ukma.cs.services.*;
import ua.edu.ukma.cs.encryption.AesEncryptionService;
import ua.edu.ukma.cs.tcp.decoders.PacketDecoder;
import ua.edu.ukma.cs.tcp.encoders.PacketEncoder;

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
    private final MainMenuPage mainMenuPage;
    private final GamePage gamePage;

    public App() {
        setTitle("Game App");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        registerPage = new RegisterPage(this, registerService);
        loginPage = new LoginPage(this, loginService);
        mainMenuPage = new MainMenuPage(
            this,
            new CreateLobbyService(httpService),
            new JoinLobbyService(httpService, new PacketEncoder(), new PacketDecoder(), new AesEncryptionService(), new RsaEncryptionService())
        );
        gamePage = new GamePage();

        cards.add(registerPage, "register");
        cards.add(loginPage, "login");
        cards.add(mainMenuPage, "lobby");
        cards.add(gamePage, "game");

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
        mainMenuPage.init();
        cardLayout.show(cards, "lobby");
    }

    public void showGame(LobbyConnection lobbyConnection) {
        gamePage.setConnection(lobbyConnection);
        gamePage.init();
        cardLayout.show(cards, "game");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
