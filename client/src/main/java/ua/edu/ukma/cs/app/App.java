package ua.edu.ukma.cs.app;

import ua.edu.ukma.cs.connection.LobbyConnection;
import ua.edu.ukma.cs.pages.*;
import ua.edu.ukma.cs.services.*;
import ua.edu.ukma.cs.encryption.AesEncryptionService;
import ua.edu.ukma.cs.tcp.decoders.PacketDecoder;
import ua.edu.ukma.cs.tcp.encoders.PacketEncoder;

import javax.swing.*;
import java.awt.*;

public class App extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private final RegisterPage registerPage;
    private final LoginPage loginPage;
    private final MainMenuPage mainMenuPage;
    private final GamePage gamePage;

    public App() {
        setTitle("Ping Pong Game");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        HttpService httpService = new HttpService();
        LoginService loginService = new LoginService(httpService);
        RegisterService registerService = new RegisterService(httpService);

        loginPage = new LoginPage(this, loginService);
        registerPage = new RegisterPage(this, registerService);
        mainMenuPage = new MainMenuPage(
            this,
            new CreateLobbyService(httpService),
            new JoinLobbyService(httpService, new PacketEncoder(), new PacketDecoder(), new AesEncryptionService(), new RsaEncryptionService())
        );
        gamePage = new GamePage();

        cards.add(loginPage, "login");
        cards.add(registerPage, "register");
        cards.add(mainMenuPage, "lobby");
        cards.add(gamePage, "game");

        add(cards);
        showLogin();
    }

    public void showLogin() {
        loginPage.init();
        cardLayout.show(cards, "login");
    }

    public void showRegister() {
        registerPage.init();
        cardLayout.show(cards, "register");
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
