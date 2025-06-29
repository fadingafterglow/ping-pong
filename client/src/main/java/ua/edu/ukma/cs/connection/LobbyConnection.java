package ua.edu.ukma.cs.connection;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
public class LobbyConnection {

    private final String host;
    private final int port;
    private final int maxPacketSize;
    private final int synchronousReadTimeoutMs;
    private final int writeQueueSize;

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

    private Consumer<LobbyConnection> onLobbyUpdate;
    private Consumer<LobbyConnection> onGameUpdate;
    private Runnable onDisconnect;

    public LobbyConnection(IEncoder<PacketOut> encoder, IDecoder<PacketIn> decoder,
                           IAsymmetricEncryptionService asymmetricEncryptionService,
                           ISymmetricEncryptionService symmetricEncryptionService, Properties properties)
    {
        this.host = properties.getProperty("game.server.host");
        this.port = Integer.parseInt(properties.getProperty("game.server.port", "10101"));
        this.maxPacketSize = Integer.parseInt(properties.getProperty("game.server.maxPacketSize", "1024"));
        this.synchronousReadTimeoutMs = Integer.parseInt(properties.getProperty("game.server.synchronousReadTimeoutMs", "1000"));
        this.writeQueueSize = Integer.parseInt(properties.getProperty("game.server.writeQueueSize", "5"));
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
        socket.connect(new InetSocketAddress(host, port)).get();
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
        ByteBuffer buffer = ByteBuffer.allocate(maxPacketSize);
        socket.read(buffer, buffer, new ReadHandler());
    }

    public void disconnect() {
        disconnect(true);
    }

    public void disconnect(boolean runCallback) {
        if (socket == null)
            return;
        writeQueue.clear();
        try {
            socket.close();
        } catch (IOException exception) {
            log.error("Failed to close socket", exception);
        }
        socket = null;
        key = null;
        if (onDisconnect != null && runCallback)
            onDisconnect.run();
    }

    public void setOnLobbyUpdateCallback(Consumer<LobbyConnection> onLobbyUpdate) {
        this.onLobbyUpdate = onLobbyUpdate;
    }

    public void setOnGameUpdateCallback(Consumer<LobbyConnection> onGameUpdate) {
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
        if (writeQueue.size() > writeQueueSize)
            writeQueue.poll();
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
        try {
            socket.write(buffer, buffer, new WriteHandler());
        } catch (NullPointerException e) {
            // expected if the socket was closed
        }
    }

    @SneakyThrows
    private PacketIn readSynchronously(AsynchronousSocketChannel socket) {
        ByteBuffer buffer = ByteBuffer.allocate(maxPacketSize);
        while (true) {
            try {
                socket.read(buffer).get(synchronousReadTimeoutMs, TimeUnit.MILLISECONDS);
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
            onLobbyUpdate.accept(this);
    }

    @SneakyThrows
    private void handleGameUpdate(byte[] payload) {
        gameState = ObjectMapperHolder.get().readValue(payload, GameStateSnapshot.class);
        if (onGameUpdate != null)
            onGameUpdate.accept(this);
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
        public void failed(Throwable exc, ByteBuffer buffer) {
            if (exc instanceof AsynchronousCloseException)
                return;
            log.error("Failed to write to server", exc);
            disconnect();
        }
    }

    private class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

        private final ByteBuffer readBuffer;

        public ReadHandler() {
            this.readBuffer = ByteBuffer.allocate(maxPacketSize * 2);
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
            try {
                socket.read(buffer, buffer, this);
            } catch (NullPointerException e) {
                // expected if the socket was closed
            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer buffer) {
            if (exc instanceof AsynchronousCloseException)
                return;
            log.error("Failed to read from server", exc);
            disconnect();
        }
    }
}
