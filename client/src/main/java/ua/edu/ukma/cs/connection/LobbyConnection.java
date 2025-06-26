package ua.edu.ukma.cs.connection;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ua.edu.ukma.cs.app.AppState;
import ua.edu.ukma.cs.encryption.ISymmetricEncryptionService;
import ua.edu.ukma.cs.game.lobby.GameLobbySnapshot;
import ua.edu.ukma.cs.game.state.GameStateSnapshot;
import ua.edu.ukma.cs.services.IAsymmetricEncryptionService;
import ua.edu.ukma.cs.tcp.decoders.IDecoder;
import ua.edu.ukma.cs.tcp.encoders.IEncoder;
import ua.edu.ukma.cs.tcp.packets.PacketIn;
import ua.edu.ukma.cs.tcp.packets.PacketOut;
import ua.edu.ukma.cs.tcp.packets.PacketType;
import ua.edu.ukma.cs.tcp.packets.payload.JoinLobbyRequest;
import ua.edu.ukma.cs.tcp.packets.payload.JoinLobbyResponse;
import ua.edu.ukma.cs.tcp.packets.payload.MoveRacketRequest;
import ua.edu.ukma.cs.utils.ObjectMapperHolder;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class LobbyConnection {

    private static final String HOST = "localhost";
    private static final int PORT = 10101;
    private static final int MAX_PACKET_SIZE = 1024;
    private static final int SYNCHRONOUS_READ_TIMEOUT_MS = 1000;

    private final IEncoder<PacketOut> encoder;
    private final IDecoder<PacketIn> decoder;
    private final IAsymmetricEncryptionService asymmetricEncryptionService;
    private final ISymmetricEncryptionService symmetricEncryptionService;
    private final ConcurrentLinkedQueue<ByteBuffer> writeQueue;
    private final AtomicBoolean isWriting;

    private AsynchronousSocketChannel socket;
    private byte[] key;

    @Getter
    private volatile GameLobbySnapshot lobbyState;
    @Getter
    private volatile GameStateSnapshot gameState;

    private Runnable onLobbyUpdate;
    private Runnable onGameUpdate;
    private Runnable onDisconnect;

    public LobbyConnection(IEncoder<PacketOut> encoder, IDecoder<PacketIn> decoder,
                           IAsymmetricEncryptionService asymmetricEncryptionService,
                           ISymmetricEncryptionService symmetricEncryptionService)
    {
        this.encoder = encoder;
        this.decoder = decoder;
        this.asymmetricEncryptionService = asymmetricEncryptionService;
        this.symmetricEncryptionService = symmetricEncryptionService;
        this.writeQueue = new ConcurrentLinkedQueue<>();
        this.isWriting = new AtomicBoolean();
    }

    @SneakyThrows
    public void init(UUID lobbyId, String token, byte[] publicKey) {
        if (socket != null) return;
        socket = AsynchronousSocketChannel.open();
        socket.connect(new InetSocketAddress(HOST, PORT)).get();
        key = symmetricEncryptionService.generateKey();

        JoinLobbyRequest joinLobbyRequest = new JoinLobbyRequest(lobbyId, token, key);
        byte[] payload = asymmetricEncryptionService.encrypt(ObjectMapperHolder.get().writeValueAsBytes(joinLobbyRequest), publicKey);
        enqueueWrite(new PacketOut(PacketType.JOIN_LOBBY_REQUEST, payload));

        PacketIn response = readSynchronously(socket);
        if (response == null || response.getType() != PacketType.JOIN_LOBBY_RESPONSE) {
            disconnect();
            throw new RuntimeException("Failed to join lobby");
        }
        JoinLobbyResponse joinLobbyResponse = ObjectMapperHolder.get().readValue(symmetricEncryptionService.decrypt(response.getData(), key), JoinLobbyResponse.class);
        if (!joinLobbyResponse.isSuccess()) {
            disconnect();
            throw new RuntimeException(joinLobbyResponse.getMessage());
        }

        lobbyState = joinLobbyResponse.getLobby();
        socket.read(ByteBuffer.allocate(MAX_PACKET_SIZE), null, new ReadHandler());
    }

    @SneakyThrows
    public void disconnect() {
        if (socket == null)
            return;
        socket.close();
        socket = null;
        key = null;
        if (onDisconnect != null)
            onDisconnect.run();
    }

    public void setOnLobbyUpdateCallback(Runnable onLobbyUpdate) {
        this.onLobbyUpdate = onLobbyUpdate;
    }

    public void setOnGameUpdateCallback(Runnable onGameUpdate) {
        this.onGameUpdate = onGameUpdate;
    }

    public void setOnDisconnectCallback(Runnable onDisconnect) {
        this.onDisconnect = onDisconnect;
    }

    public void sendStartGameRequest() {
        PacketOut packetOut = new PacketOut(PacketType.START_GAME_REQUEST, new byte[0]);
        enqueueWrite(packetOut);
    }

    @SneakyThrows
    public void sendMoveRacketRequest(boolean up) {
        MoveRacketRequest moveRacketRequest = new MoveRacketRequest(up);
        byte[] payload = symmetricEncryptionService.encrypt(ObjectMapperHolder.get().writeValueAsBytes(moveRacketRequest), key);
        PacketOut packetOut = new PacketOut(PacketType.MOVE_RACKET_REQUEST, payload);
        enqueueWrite(packetOut);
    }

    private void enqueueWrite(PacketOut packet) {
        ByteBuffer buffer = encoder.encode(packet);
        buffer.flip();
        writeQueue.offer(buffer);
        tryWrite();
    }

    private void tryWrite() {
        if (isWriting.compareAndSet(false, true))
            write();
    }

    private void write() {
        ByteBuffer buffer = writeQueue.poll();
        if (buffer == null) {
            isWriting.set(false);
            if (!writeQueue.isEmpty())
                tryWrite();
            return;
        }
        socket.write(buffer, buffer, new WriteHandler());
    }

    @SneakyThrows
    private PacketIn readSynchronously(AsynchronousSocketChannel socket) {
        ByteBuffer buffer = ByteBuffer.allocate(MAX_PACKET_SIZE);
        while (true) {
            try {
                socket.read(buffer).get(SYNCHRONOUS_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (TimeoutException ex) {
                return null;
            }
            buffer.flip();
            PacketIn packetIn = decoder.decode(buffer);
            if (packetIn != null)
                return packetIn;
            buffer.compact();
        }
    }

    @SneakyThrows
    private void handleLobbyUpdate(byte[] payload) {
        lobbyState = ObjectMapperHolder.get().readValue(payload, GameLobbySnapshot.class);
        if (onLobbyUpdate != null)
            onLobbyUpdate.run();
    }

    @SneakyThrows
    private void handleGameUpdate(byte[] payload) {
        gameState = ObjectMapperHolder.get().readValue(payload, GameStateSnapshot.class);
        if (onGameUpdate != null)
            onGameUpdate.run();
    }

    private class WriteHandler implements CompletionHandler<Integer, ByteBuffer> {

        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            if (buffer.hasRemaining()) {
                socket.write(buffer, buffer, this);
                return;
            }
            write();
        }

        @Override
        @SneakyThrows
        public void failed(Throwable exc, ByteBuffer buffer) {
            log.error("Failed to write to server", exc);
            disconnect();
        }
    }

    private class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

        private final ByteBuffer readBuffer;

        public ReadHandler() {
            this.readBuffer = ByteBuffer.allocate(MAX_PACKET_SIZE * 2);
        }

        @Override
        public void completed(Integer result, ByteBuffer buffer) {
            if (result == -1)
                disconnect();
            buffer.flip();
            readBuffer.put(buffer);

            readBuffer.flip();
            while (true) {
                try {
                    PacketIn packet = decoder.decode(readBuffer);
                    if (packet == null)
                        break;
                    byte[] payload = symmetricEncryptionService.decrypt(packet.getData(), key);
                    switch (packet.getType()) {
                        case GAME_LOBBY_STATE_UPDATE ->  handleLobbyUpdate(payload);
                        case GAME_STATE_UPDATE -> handleGameUpdate(payload);
                    }
                } catch (Exception e) {
                    log.error("Cannot handle packet", e);
                }
            }
            readBuffer.compact();

            buffer.clear();
            socket.read(buffer, buffer, this);
        }

        @Override
        @SneakyThrows
        public void failed(Throwable exc, ByteBuffer buffer) {
            log.error("Failed to read from server", exc);
            disconnect();
        }
    }
}
