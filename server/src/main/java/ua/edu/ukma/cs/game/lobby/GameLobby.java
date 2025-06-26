package ua.edu.ukma.cs.game.lobby;

import lombok.Getter;
import lombok.Setter;
import ua.edu.ukma.cs.game.configuration.GameConfiguration;
import ua.edu.ukma.cs.game.state.GameState;
import ua.edu.ukma.cs.game.state.GameStateSnapshot;
import ua.edu.ukma.cs.security.SecurityContext;
import ua.edu.ukma.cs.tcp.connection.AsynchronousConnection;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    private String creatorUsername;
    private AsynchronousConnection creatorConnection;
    private final ConcurrentLinkedQueue<Boolean> creatorInputBuffer;

    private Integer otherPlayerId;
    private String otherPlayerUsername;
    private AsynchronousConnection otherPlayerConnection;
    private final ConcurrentLinkedQueue<Boolean> otherPlayerInputBuffer;

    private final int inputBufferSize;

    @Setter
    private ScheduledFuture<?> updater;

    @Getter
    private GameLobbyState lobbyState;
    private GameState gameState;

    public GameLobby(UUID id, int creatorId, int inputBufferSize) {
        this.id = id;
        this.creatorId = creatorId;
        this.creatorInputBuffer = new ConcurrentLinkedQueue<>();
        this.otherPlayerInputBuffer = new ConcurrentLinkedQueue<>();
        this.inputBufferSize = inputBufferSize;
        this.lobbyState = GameLobbyState.WAITING;
    }

    public boolean join(SecurityContext playerContext, AsynchronousConnection connection) {
        if (playerContext.getUserId() == creatorId) {
            if (creatorConnection == null || creatorConnection.isClosed()) {
                creatorConnection = connection;
                creatorUsername = playerContext.getUsername();
                return true;
            }
        } else if (otherPlayerId == null || playerContext.getUserId() == otherPlayerId) {
            if (otherPlayerConnection == null || otherPlayerConnection.isClosed()) {
                otherPlayerConnection = connection;
                otherPlayerId = playerContext.getUserId();
                otherPlayerUsername = playerContext.getUsername();
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
        Boolean creatorInput = creatorInputBuffer.poll();
        if (creatorInput != null)
            gameState.moveRacket(creatorInput, true);
        Boolean otherPlayerInput = otherPlayerInputBuffer.poll();
        if (otherPlayerInput != null)
            gameState.moveRacket(otherPlayerInput, false);
        boolean shouldContinue = gameState.update();
        if (!shouldContinue) {
            lobbyState = GameLobbyState.FINISHED;
            updater.cancel(false);
        }
        return shouldContinue;
    }

    public void addMove(int userId, boolean isUp) {
        if (userId == creatorId)
            addMove(creatorInputBuffer, isUp);
        else
            addMove(otherPlayerInputBuffer, isUp);
    }

    private void addMove(ConcurrentLinkedQueue<Boolean> buffer, boolean isUp) {
        buffer.offer(isUp);
        if (buffer.size() > inputBufferSize)
            buffer.poll();
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
        return GameLobbySnapshot.builder()
                .state(lobbyState)
                .creatorId(creatorId)
                .creatorUsername(creatorUsername)
                .otherPlayerId(otherPlayerId)
                .otherPlayerUsername(otherPlayerUsername)
                .gameConfiguration(DEFAULT_CONFIGURATION)
                .build();
    }

    public GameStateSnapshot takeGameSnapshot() {
        return gameState.takeSnapshot();
    }
}
