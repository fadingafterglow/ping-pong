package ua.edu.ukma.cs.game.lobby;

import ua.edu.ukma.cs.game.configuration.GameConfiguration;
import ua.edu.ukma.cs.game.state.GameState;
import ua.edu.ukma.cs.tcp.connection.AsynchronousConnection;

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

    private final Object joinLock;

    private final int creatorId;
    private AsynchronousConnection creatorConnection;

    private Integer otherPlayerId;
    private AsynchronousConnection otherPlayerConnection;

    private GameLobbyState lobbyState;
    private GameState state;

    public GameLobby(int creatorId) {
        this.creatorId = creatorId;
        this.lobbyState = GameLobbyState.WAITING;
        this.joinLock = new Object();
    }

    public boolean join(int userId, AsynchronousConnection connection) {
        synchronized (joinLock) {
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
    }

    public GameLobbyState getState() {
        return lobbyState;
    }

    public GameLobbySnapshot takeSnapshot() {
        return GameLobbySnapshot.builder()
                .state(lobbyState)
                .creatorId(creatorId)
                .otherPlayerId(otherPlayerId)
                .gameConfiguration(DEFAULT_CONFIGURATION)
                .build();
    }
}
