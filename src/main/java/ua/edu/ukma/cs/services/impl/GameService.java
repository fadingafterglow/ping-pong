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
import ua.edu.ukma.cs.services.IAsymmetricEncryptionService;
import ua.edu.ukma.cs.services.IGameService;
import ua.edu.ukma.cs.services.ISymmetricEncryptionService;
import ua.edu.ukma.cs.tcp.connection.AsynchronousConnection;
import ua.edu.ukma.cs.tcp.handlers.ITcpRequestHandler;
import ua.edu.ukma.cs.tcp.packets.PacketIn;
import ua.edu.ukma.cs.tcp.packets.PacketOut;
import ua.edu.ukma.cs.tcp.packets.PacketType;
import ua.edu.ukma.cs.tcp.packets.payload.JoinLobbyRequest;
import ua.edu.ukma.cs.tcp.packets.payload.JoinLobbyResponse;
import ua.edu.ukma.cs.utils.SharedObjectMapper;
import ua.edu.ukma.cs.validation.Validator;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GameService implements IGameService, ITcpRequestHandler {

    private static final String KEY_ATTRIBUTE = "key";
    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String LOBBY_ID_ATTRIBUTE = "lobbyId";

    private final JwtServices jwtServices;
    private final IAsymmetricEncryptionService asymmetricEncryptionService;
    private final ISymmetricEncryptionService symmetricEncryptionService;

    private final Cache<UUID, GameLobby> lobbies;
    private final int startDelay;
    private final int updateInterval;
    private final ScheduledExecutorService gameScheduler;

    public GameService(JwtServices jwtServices, IAsymmetricEncryptionService asymmetricEncryptionService,
                       ISymmetricEncryptionService symmetricEncryptionService, Properties properties)
    {
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
    }

    @Override
    public UUID createLobby(int creatorId) {
        UUID lobbyId = UUID.randomUUID();
        GameLobby lobby = new GameLobby(lobbyId, creatorId);
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
                    handleStartGameRequest(connection.getAttribute(LOBBY_ID_ATTRIBUTE), connection.getAttribute(USER_ID_ATTRIBUTE));
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
        int userId;
        try {
            userId = jwtServices.verifyToken(request.getUserJwt()).getUserId();
        }
        catch (JWTVerificationException e) {
            return new JoinLobbyResponse("Authentication failed");
        }
        synchronized (lobby) {
            if (lobby.getLobbyState() == GameLobbyState.FINISHED)
                return new JoinLobbyResponse("Lobby is finished");
            boolean hasJoined = lobby.join(userId, connection);
            if (hasJoined) {
                connection.setAttribute(USER_ID_ATTRIBUTE, userId);
                connection.setAttribute(LOBBY_ID_ATTRIBUTE, lobbyId);
                sendGameLobbyStateUpdate(lobby.getOtherConnection(userId), lobby.takeLobbySnapshot());
                return new JoinLobbyResponse(lobby.takeLobbySnapshot(true));
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
        return SharedObjectMapper.S.readValue(decryptor.decrypt(data), type);
    }

    @SneakyThrows
    private <T> byte[] serializePayload(T payload, Encryptor encryptor) {
        return encryptor.encrypt(SharedObjectMapper.S.writeValueAsBytes(payload));
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
