package ua.edu.ukma.cs.app;

import lombok.Getter;
import ua.edu.ukma.cs.connection.LobbyConnection;
import ua.edu.ukma.cs.pages.*;
import ua.edu.ukma.cs.services.*;
import ua.edu.ukma.cs.encryption.AesEncryptionService;
import ua.edu.ukma.cs.tcp.decoders.PacketDecoder;
import ua.edu.ukma.cs.tcp.encoders.PacketEncoder;

import javax.swing.*;
import java.awt.*;

public class PingPongClient extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private final RegisterPage registerPage;
    private final LoginPage loginPage;
    private final MainMenuPage mainMenuPage;
    private final LobbyPage lobbyPage;
    private final GamePage gamePage;

    @Getter
    private final AppState appState;

    public PingPongClient() {
        setTitle("Ping Pong Game");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        appState = new AppState();

        HttpService httpService = new HttpService(appState);
        LoginService loginService = new LoginService(httpService);
        RegisterService registerService = new RegisterService(httpService);
        CreateLobbyService createLobbyService = new CreateLobbyService(httpService);
        JoinLobbyService joinLobbyService = new JoinLobbyService(httpService, new PacketEncoder(), new PacketDecoder(), new AesEncryptionService(), new RsaEncryptionService());

        loginPage = new LoginPage(this, loginService);
        registerPage = new RegisterPage(this, registerService);
        mainMenuPage = new MainMenuPage(this, createLobbyService, joinLobbyService);
        lobbyPage = new LobbyPage(this);
        gamePage = new GamePage(this);

        cards.add(loginPage, LoginPage.class.getSimpleName());
        cards.add(registerPage, RegisterPage.class.getSimpleName());
        cards.add(mainMenuPage, MainMenuPage.class.getSimpleName());
        cards.add(lobbyPage, LobbyPage.class.getSimpleName());
        cards.add(gamePage, GamePage.class.getSimpleName());

        add(cards);
        showLogin();
    }

    public void showLogin() {
        loginPage.init();
        cardLayout.show(cards, LoginPage.class.getSimpleName());
    }

    public void showRegister() {
        registerPage.init();
        cardLayout.show(cards, RegisterPage.class.getSimpleName());
    }

    public void showMainMenu() {
        mainMenuPage.init();
        cardLayout.show(cards, MainMenuPage.class.getSimpleName());
    }

    public void showLobby() {
        lobbyPage.init();
        cardLayout.show(cards, LobbyPage.class.getSimpleName());
    }

    public void showGame() {
        gamePage.init();
        cardLayout.show(cards, GamePage.class.getSimpleName());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PingPongClient().setVisible(true));
    }
}
