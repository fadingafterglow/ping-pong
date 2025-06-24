package ua.edu.ukma.cs.game.lobby;

import lombok.Getter;
import lombok.Setter;
import ua.edu.ukma.cs.game.configuration.GameConfiguration;
import ua.edu.ukma.cs.game.state.GameState;
import ua.edu.ukma.cs.game.state.GameStateSnapshot;
import ua.edu.ukma.cs.tcp.connection.AsynchronousConnection;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;

public class GameLobby {

    private static final GameConfiguration DEFAULT_CONFIGURATION =
        GameConfiguration.builder()
            .fieldWidth(800)
            .fieldHeight(600)
            .racketWidth(10)
            .racketHeight(100)
            .racketSpeed(4)
            .ballRadius(8)
            .initialBallSpeed(5)
            .maxScore(13)
            .build();

    @Getter
    private final UUID id;

    @Getter
    private final int creatorId;
    private AsynchronousConnection creatorConnection;

    private Integer otherPlayerId;
    private AsynchronousConnection otherPlayerConnection;

    @Setter
    private ScheduledFuture<?> updater;

    @Getter
    private GameLobbyState lobbyState;
    private GameState gameState;

    public GameLobby(UUID id, int creatorId) {
        this.id = id;
        this.creatorId = creatorId;
        this.lobbyState = GameLobbyState.WAITING;
    }

    public boolean join(int userId, AsynchronousConnection connection) {
        if (userId == creatorId) {
            if (creatorConnection == null || creatorConnection.isClosed()) {
                creatorConnection = connection;
                return true;
            }
        } else if (otherPlayerId == null || otherPlayerId == userId) {
            if (otherPlayerConnection == null || otherPlayerConnection.isClosed()) {
                otherPlayerId = userId;
                otherPlayerConnection = connection;
                return true;
            }
        }
        return false;
    }

    public boolean startGame() {
        if (lobbyState != GameLobbyState.WAITING || otherPlayerId == null)
            return false;
        lobbyState = GameLobbyState.IN_PROGRESS;
        gameState = new GameState(DEFAULT_CONFIGURATION);
        return true;
    }

    public boolean updateGameState() {
        boolean shouldContinue = gameState.update();
        if (!shouldContinue) {
            lobbyState = GameLobbyState.FINISHED;
            updater.cancel(false);
        }
        return shouldContinue;
    }

    public AsynchronousConnection getConnection(int userId) {
        if (userId == creatorId)
            return creatorConnection;
        return otherPlayerConnection;
    }

    public AsynchronousConnection getOtherConnection(int userId) {
        if (userId == creatorId)
            return otherPlayerConnection;
        return creatorConnection;
    }

    public GameLobbySnapshot takeLobbySnapshot() {
        return takeLobbySnapshot(false);
    }

    public GameLobbySnapshot takeLobbySnapshot(boolean withConfiguration) {
        return GameLobbySnapshot.builder()
                .state(lobbyState)
                .creatorId(creatorId)
                .otherPlayerId(otherPlayerId)
                .gameConfiguration(withConfiguration ? DEFAULT_CONFIGURATION : null)
                .build();
    }

    public GameStateSnapshot takeGameSnapshot() {
        return gameState.takeSnapshot();
    }
}
