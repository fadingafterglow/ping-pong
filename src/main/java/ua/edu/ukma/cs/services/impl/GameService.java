package ua.edu.ukma.cs.services.impl;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import ua.edu.ukma.cs.exception.ValidationException;
import ua.edu.ukma.cs.game.lobby.GameLobby;
import ua.edu.ukma.cs.game.lobby.GameLobbyState;
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
import java.util.concurrent.TimeUnit;

public class GameService implements IGameService, ITcpRequestHandler {

    private static final String KEY_ATTRIBUTE = "key";
    private static final String USER_ID_ATTRIBUTE = "userId";

    private final JwtServices jwtServices;
    private final IAsymmetricEncryptionService asymmetricEncryptionService;
    private final ISymmetricEncryptionService symmetricEncryptionService;

    private final Cache<UUID, GameLobby> lobbies;

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
    }

    @Override
    public UUID createLobby(int creatorId) {
        GameLobby lobby = new GameLobby(creatorId);
        UUID lobbyId = UUID.randomUUID();
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
            }
        } catch (GeneralSecurityException | IOException | ValidationException ex) {
            sendResponse(connection, PacketType.BAD_PAYLOAD_RESPONSE);
        }
    }

    private JoinLobbyResponse handleJoinLobbyRequest(JoinLobbyRequest request, AsynchronousConnection connection) {
        UUID lobbyId = request.getGameLobbyId();
        GameLobby lobby = lobbies.getIfPresent(lobbyId);
        if (lobby == null || lobby.getState() == GameLobbyState.FINISHED)
            return new JoinLobbyResponse("Lobby not exists or is finished");
        int userId;
        try {
            userId = jwtServices.verifyToken(request.getUserJwt()).getUserId();
        }
        catch (JWTVerificationException e) {
            return new JoinLobbyResponse("Authentication failed");
        }
        boolean hasJoined = lobby.join(userId, connection);
        if (hasJoined) {
            connection.setAttribute(USER_ID_ATTRIBUTE, userId);
            return new JoinLobbyResponse(lobby.takeSnapshot());
        }
        return new JoinLobbyResponse("User already in the lobby or it is full");
    }

    private void validate(JoinLobbyRequest request) {
        Validator.validate(request)
                .notNull(JoinLobbyRequest::getGameLobbyId)
                .notBlank(JoinLobbyRequest::getUserJwt)
                .notNull(JoinLobbyRequest::getSymmetricKey);
        symmetricEncryptionService.validateKey(request.getSymmetricKey());
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
