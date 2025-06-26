package ua.edu.ukma.cs.services.impl;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import ua.edu.ukma.cs.exception.ValidationException;
import ua.edu.ukma.cs.game.lobby.GameLobby;
import ua.edu.ukma.cs.game.lobby.GameLobbySnapshot;
import ua.edu.ukma.cs.game.lobby.GameLobbyState;
import ua.edu.ukma.cs.game.state.GameStateSnapshot;
import ua.edu.ukma.cs.security.JwtServices;
import ua.edu.ukma.cs.security.SecurityContext;
import ua.edu.ukma.cs.services.IAsymmetricDecryptionService;
import ua.edu.ukma.cs.services.IGameResultService;
import ua.edu.ukma.cs.services.IGameService;
import ua.edu.ukma.cs.encryption.ISymmetricEncryptionService;
import ua.edu.ukma.cs.tcp.connection.AsynchronousConnection;
import ua.edu.ukma.cs.tcp.handlers.ITcpRequestHandler;
import ua.edu.ukma.cs.tcp.packets.PacketIn;
import ua.edu.ukma.cs.tcp.packets.PacketOut;
import ua.edu.ukma.cs.tcp.packets.PacketType;
import ua.edu.ukma.cs.tcp.packets.payload.JoinLobbyRequest;
import ua.edu.ukma.cs.tcp.packets.payload.JoinLobbyResponse;
import ua.edu.ukma.cs.tcp.packets.payload.MoveRacketRequest;
import ua.edu.ukma.cs.utils.ObjectMapperHolder;
import ua.edu.ukma.cs.validation.Validator;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;

public class GameService implements IGameService, ITcpRequestHandler {

    private static final String KEY_ATTRIBUTE = "key";
    private static final String SECURITY_CONTEXT_ATTRIBUTE = "securityContext";
    private static final String LOBBY_ID_ATTRIBUTE = "lobbyId";

    private final IGameResultService gameResultService;
    private final JwtServices jwtServices;
    private final IAsymmetricDecryptionService asymmetricEncryptionService;
    private final ISymmetricEncryptionService symmetricEncryptionService;

    private final Cache<UUID, GameLobby> lobbies;
    private final int startDelay;
    private final int updateInterval;
    private final ScheduledExecutorService gameScheduler;
    private final int inputBufferSize;
    private final ExecutorService resultsSaver;

    public GameService(IGameResultService gameResultService, JwtServices jwtServices,
                       IAsymmetricDecryptionService asymmetricEncryptionService, ISymmetricEncryptionService symmetricEncryptionService,
                       Properties properties)
    {
        this.gameResultService = gameResultService;
        this.jwtServices = jwtServices;
        this.asymmetricEncryptionService = asymmetricEncryptionService;
        this.symmetricEncryptionService = symmetricEncryptionService;

        int idleTimeout = Integer.parseInt(properties.getProperty("game.lobby.idleTimeout", "15"));
        this.lobbies = CacheBuilder.newBuilder()
                .expireAfterAccess(idleTimeout, TimeUnit.MINUTES)
                .build();
        this.startDelay = Integer.parseInt(properties.getProperty("game.lobby.startDelay", "5000"));
        this.updateInterval = Integer.parseInt(properties.getProperty("game.lobby.updateInterval", "15"));
        int schedulerCoreThreads = Integer.parseInt(properties.getProperty("game.scheduler.coreThreads", "2"));
        this.gameScheduler = Executors.newScheduledThreadPool(schedulerCoreThreads);
        this.inputBufferSize = Integer.parseInt(properties.getProperty("game.input.bufferSize", "3"));
        this.resultsSaver = Executors.newCachedThreadPool();
    }

    @Override
    public UUID createLobby(int creatorId) {
        UUID lobbyId = UUID.randomUUID();
        GameLobby lobby = new GameLobby(lobbyId, creatorId, inputBufferSize);
        lobbies.put(lobbyId, lobby);
        return lobbyId;
    }

    @Override
    public void handle(PacketIn packet, AsynchronousConnection connection) {
        try {
            switch (packet.getType()) {
                case JOIN_LOBBY_REQUEST -> {
                    JoinLobbyRequest request = extractPayload(packet.getData(), asymmetricEncryptionService::decrypt, JoinLobbyRequest.class);
                    validate(request);
                    connection.setAttribute(KEY_ATTRIBUTE, request.getSymmetricKey());
                    JoinLobbyResponse response = handleJoinLobbyRequest(request, connection);
                    sendResponse(connection, PacketType.JOIN_LOBBY_RESPONSE, response);
                    if (!response.isSuccess())
                        connection.setAttribute(KEY_ATTRIBUTE, null);
                }
                case START_GAME_REQUEST ->
                    handleStartGameRequest(connection.getAttribute(LOBBY_ID_ATTRIBUTE), connection.<SecurityContext>getAttribute(SECURITY_CONTEXT_ATTRIBUTE).getUserId());
                case MOVE_RACKET_REQUEST -> {
                    byte[] key = connection.getAttribute(KEY_ATTRIBUTE);
                    MoveRacketRequest request = extractPayload(packet.getData(), d -> symmetricEncryptionService.decrypt(d, key), MoveRacketRequest.class);
                    handleMoveRacketRequest(request, connection.getAttribute(LOBBY_ID_ATTRIBUTE), connection.<SecurityContext>getAttribute(SECURITY_CONTEXT_ATTRIBUTE).getUserId());
                }
            }
        } catch (GeneralSecurityException | IOException | ValidationException ex) {
            sendResponse(connection, PacketType.BAD_PAYLOAD_RESPONSE);
        }
    }

    private void validate(JoinLobbyRequest request) {
        Validator.validate(request)
                .notNull(JoinLobbyRequest::getGameLobbyId)
                .notBlank(JoinLobbyRequest::getUserJwt)
                .notNull(JoinLobbyRequest::getSymmetricKey);
        symmetricEncryptionService.validateKey(request.getSymmetricKey());
    }

    private JoinLobbyResponse handleJoinLobbyRequest(JoinLobbyRequest request, AsynchronousConnection connection) {
        UUID lobbyId = request.getGameLobbyId();
        GameLobby lobby = lobbies.getIfPresent(lobbyId);
        if (lobby == null)
            return new JoinLobbyResponse("Lobby not exists");
        SecurityContext playerContext;
        try {
            playerContext = jwtServices.verifyToken(request.getUserJwt());
        }
        catch (JWTVerificationException e) {
            return new JoinLobbyResponse("Authentication failed");
        }
        synchronized (lobby) {
            if (lobby.getLobbyState() == GameLobbyState.FINISHED)
                return new JoinLobbyResponse("Lobby is finished");
            boolean hasJoined = lobby.join(playerContext, connection);
            if (hasJoined) {
                connection.setAttribute(SECURITY_CONTEXT_ATTRIBUTE, playerContext);
                connection.setAttribute(LOBBY_ID_ATTRIBUTE, lobbyId);
                sendGameLobbyStateUpdate(lobby.getOtherConnection(playerContext.getUserId()), lobby.takeLobbySnapshot());
                return new JoinLobbyResponse(lobby.takeLobbySnapshot());
            }
        }
        return new JoinLobbyResponse("User already in the lobby or it is full");
    }

    private void handleStartGameRequest(UUID lobbyId, int userId) {
        GameLobby lobby = lobbies.getIfPresent(lobbyId);
        if (lobby == null) return;
        synchronized (lobby) {
            if (lobby.getCreatorId() != userId)
                return;
            boolean hasStarted = lobby.startGame();
            if (hasStarted) {
                GameLobbySnapshot snapshot = lobby.takeLobbySnapshot();
                sendGameLobbyStateUpdate(lobby.getConnection(userId), snapshot);
                sendGameLobbyStateUpdate(lobby.getOtherConnection(userId), snapshot);
                ScheduledFuture<?> updater = gameScheduler.scheduleWithFixedDelay(() -> updateGame(lobby), startDelay, updateInterval, TimeUnit.MILLISECONDS);
                lobby.setUpdater(updater);
            }
        }
    }

    private void handleMoveRacketRequest(MoveRacketRequest request, UUID lobbyId, int userId) {
        GameLobby lobby = lobbies.getIfPresent(lobbyId);
        if (lobby == null) return;
        lobby.addMove(userId, request.isUp());
    }

    @Override
    public void handleDisconnect(AsynchronousConnection connection) {
        UUID lobbyId = connection.getAttribute(LOBBY_ID_ATTRIBUTE);
        if (lobbyId == null) return;
        GameLobby lobby = lobbies.getIfPresent(lobbyId);
        if (lobby == null) return;
        int userId = connection.<SecurityContext>getAttribute(SECURITY_CONTEXT_ATTRIBUTE).getUserId();
        synchronized (lobby) {
            if (lobby.getLobbyState() != GameLobbyState.WAITING)
                return;
            AsynchronousConnection otherConnection = lobby.getOtherConnection(userId);
            if (lobby.getCreatorId() == userId) {
                lobbies.invalidate(lobbyId);
                if (otherConnection != null)
                    otherConnection.disconnect();
            } else {
                lobby.clearOtherPlayer();
                sendGameLobbyStateUpdate(otherConnection, lobby.takeLobbySnapshot());
            }
        }
    }

    private void updateGame(GameLobby lobby) {
        boolean shouldContinue = lobby.updateGameState();
        AsynchronousConnection connection = lobby.getConnection(0);
        AsynchronousConnection otherConnection = lobby.getOtherConnection(0);
        GameStateSnapshot gameSnapshot = lobby.takeGameSnapshot();
        sendGameStateUpdate(connection, gameSnapshot);
        sendGameStateUpdate(otherConnection, gameSnapshot);
        if (!shouldContinue) {
            lobbies.invalidate(lobby.getId());
            GameLobbySnapshot lobbySnapshot = lobby.takeLobbySnapshot();
            sendGameLobbyStateUpdate(connection, lobbySnapshot);
            sendGameLobbyStateUpdate(otherConnection, lobbySnapshot);
            resultsSaver.submit(() -> gameResultService.saveGameResult(lobbySnapshot, gameSnapshot));
        }
    }

    private void sendGameLobbyStateUpdate(AsynchronousConnection connection, GameLobbySnapshot snapshot) {
        if (connection == null || connection.isClosed()) return;
        sendResponse(connection, PacketType.GAME_LOBBY_STATE_UPDATE, snapshot);
    }

    private void sendGameStateUpdate(AsynchronousConnection connection, GameStateSnapshot snapshot) {
        if (connection == null || connection.isClosed()) return;
        sendResponse(connection, PacketType.GAME_STATE_UPDATE, snapshot);
    }

    private void sendResponse(AsynchronousConnection connection, PacketType type) {
        PacketOut responsePacket = new PacketOut(type, new byte[0]);
        connection.enqueueWrite(responsePacket);
    }

    private <T> void sendResponse(AsynchronousConnection connection, PacketType type, T payload) {
        byte[] key = connection.getAttribute(KEY_ATTRIBUTE);
        byte[] data = serializePayload(payload, d -> symmetricEncryptionService.encrypt(d, key));
        PacketOut responsePacket = new PacketOut(type, data);
        connection.enqueueWrite(responsePacket);
    }

    private <T> T extractPayload(byte[] data, Decryptor decryptor, Class<T> type) throws IOException, GeneralSecurityException {
        return ObjectMapperHolder.get().readValue(decryptor.decrypt(data), type);
    }

    @SneakyThrows
    private <T> byte[] serializePayload(T payload, Encryptor encryptor) {
        return encryptor.encrypt(ObjectMapperHolder.get().writeValueAsBytes(payload));
    }

    @FunctionalInterface
    private interface Decryptor {
        byte[] decrypt(byte[] data) throws GeneralSecurityException;
    }

    @FunctionalInterface
    private interface Encryptor {
        byte[] encrypt(byte[] data);
    }
}
