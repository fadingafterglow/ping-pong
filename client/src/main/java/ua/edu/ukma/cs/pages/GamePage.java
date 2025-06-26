package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.app.AppState;
import ua.edu.ukma.cs.app.PingPongClient;
import ua.edu.ukma.cs.connection.LobbyConnection;
import ua.edu.ukma.cs.game.configuration.GameConfiguration;
import ua.edu.ukma.cs.game.lobby.GameLobbySnapshot;
import ua.edu.ukma.cs.game.lobby.GameLobbyState;
import ua.edu.ukma.cs.game.state.GameStateSnapshot;
import ua.edu.ukma.cs.utils.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GamePage extends BasePage {

    private static final double FONT_SIZE_COEFFICIENT = 0.05;
    private static final int INPUT_DELAY_MS = 10;

    private GameConfiguration config;
    private GameStateSnapshot state;
    private ScheduledExecutorService moveSender;

    private volatile boolean upPressed;
    private volatile boolean downPressed;

    public GamePage(PingPongClient app) {
        super(app);

        setBackground(Color.BLACK);

        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "downPressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "downReleased");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "upPressed");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "upReleased");

        ActionMap actionMap = getActionMap();
        actionMap.put("downPressed", actionOf(() -> downPressed = true));
        actionMap.put("downReleased", actionOf(() -> downPressed = false));
        actionMap.put("upPressed", actionOf(() -> upPressed = true));
        actionMap.put("upReleased", actionOf(() -> upPressed = false));
    }

    @Override
    public void init() {
        AppState appState = app.getAppState();
        LobbyConnection connection = appState.getLobbyConnection();
        config = connection.getLobbyState().gameConfiguration();
        state = connection.getGameState();
        connection.setOnGameUpdateCallback(this::onGameUpdate);
        connection.setOnLobbyUpdateCallback(this::onLobbyUpdate);
        moveSender = Executors.newSingleThreadScheduledExecutor();
        moveSender.scheduleWithFixedDelay(() -> sendMoveIfNeeded(connection), 0, INPUT_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    private void onGameUpdate(LobbyConnection connection) {
        this.state = connection.getGameState();
        repaint();
    }

    private void onLobbyUpdate(LobbyConnection connection) {
        GameLobbySnapshot snapshot = connection.getLobbyState();
        if (snapshot.state() != GameLobbyState.FINISHED)
            return;
        moveSender.shutdown();
        connection.setOnDisconnectCallback(null);
        connection.disconnect();
        app.getAppState().clearLobbyConnection();
        DialogUtils.gameResultsDialog(this, app.getAppState().getUserId() == getWinnerId(snapshot, connection.getGameState()));
        app.showMainMenu();
    }

    private int getWinnerId(GameLobbySnapshot lobbySnapshot, GameStateSnapshot gameSnapshot) {
        return gameSnapshot.player1Score() > gameSnapshot.player2Score() ? lobbySnapshot.creatorId() : lobbySnapshot.otherPlayerId();
    }

    private void sendMoveIfNeeded(LobbyConnection connection) {
        if (state == null) return;
        if (upPressed)
            connection.sendMoveRacketRequest(true);
        if (downPressed)
            connection.sendMoveRacketRequest(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics;

        int width = getWidth();
        int height = getHeight();
        g.setColor(Color.WHITE);

        Font font = new Font("Monospaced", Font.BOLD, scale(height, FONT_SIZE_COEFFICIENT));
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        int textHeight = metrics.getHeight();
        int ascent = metrics.getAscent();

        if (state == null) {
            String text = "Starting...";
            int textWidth = metrics.stringWidth(text);
            g.drawString(text, (width - textWidth) / 2, (height - textHeight) / 2 + ascent);
            return;
        }

        double xCoefficient = (double) width / config.fieldWidth();
        double yCoefficient = (double) height / config.fieldHeight();

        g.fillRect(
                scale(state.player1RacketX(), xCoefficient), scale(state.player1RacketY(), yCoefficient),
                scale(config.racketWidth(), xCoefficient), scale(config.racketHeight(), yCoefficient)
        );

        g.fillRect(
                scale(state.player2RacketX(), xCoefficient), scale(state.player2RacketY(), yCoefficient),
                scale(config.racketWidth(), xCoefficient), scale(config.racketHeight(), yCoefficient)
        );

        int d = config.ballRadius() * 2;
        g.fillOval(
                scale(state.ballX() - config.ballRadius(), xCoefficient),
                scale(state.ballY() - config.ballRadius(), yCoefficient),
                scale(d, xCoefficient),
                scale(d, yCoefficient)
        );

        String text = state.player1Score() + " : " + state.player2Score();
        int textWidth = metrics.stringWidth(text);
        g.drawString(text, (width - textWidth) / 2, textHeight);
    }

    private int scale(int value, double coefficient) {
        return (int) Math.round(value * coefficient);
    }

    private Action actionOf(Runnable runnable) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runnable.run();
            }
        };
    }
} 