package ua.edu.ukma.cs.servers;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ua.edu.ukma.cs.tcp.connection.AsynchronousConnection;
import ua.edu.ukma.cs.tcp.decoders.IDecoder;
import ua.edu.ukma.cs.tcp.encoders.IEncoder;
import ua.edu.ukma.cs.tcp.handlers.ITcpRequestHandler;
import ua.edu.ukma.cs.tcp.packets.PacketIn;
import ua.edu.ukma.cs.tcp.packets.PacketOut;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Properties;
import java.util.concurrent.Executors;

@Slf4j
public class GameServer implements IServer {

    private final int port;
    private final short maxPacketSize;

    private volatile boolean running;

    private final ITcpRequestHandler requestHandler;
    private final IDecoder<PacketIn> decoder;
    private final IEncoder<PacketOut> encoder;

    private AsynchronousChannelGroup group;
    private AsynchronousServerSocketChannel serverSocket;

    public GameServer(IDecoder<PacketIn> decoder, IEncoder<PacketOut> encoder, ITcpRequestHandler tcpRequestHandler, Properties properties) {
        this.requestHandler = tcpRequestHandler;
        this.decoder = decoder;
        this.encoder = encoder;

        this.port = Integer.parseInt(properties.getProperty("game.server.port", "10101"));
        this.maxPacketSize = Short.parseShort(properties.getProperty("game.server.maxPacketSize", "1024"));
    }

    @Override
    public void start() throws IOException {
        if (running) return;
        group = AsynchronousChannelGroup.withCachedThreadPool(Executors.newCachedThreadPool(), 1);
        serverSocket = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port));
        running = true;
        serverSocket.accept(serverSocket, new ConnectionHandler());
        log.info("Game server started on port {}", port);
    }

    @Override
    public void stop() throws IOException {
        if (!running) return;
        running = false;
        group.shutdown();
        serverSocket.close();
    }

    private class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, AsynchronousServerSocketChannel> {

        @Override
        public void completed(AsynchronousSocketChannel socket, AsynchronousServerSocketChannel serverSocket) {
            if (!running) return;
            ByteBuffer buffer = ByteBuffer.allocate(maxPacketSize);
            socket.read(buffer, buffer, new ReadHandler(socket));
            serverSocket.accept(serverSocket, this);
        }

        @Override
        public void failed(Throwable exc, AsynchronousServerSocketChannel serverSocket) {
            log.error("Failed to accept connection", exc);
        }
    }

    private class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {

        private final AsynchronousSocketChannel socket;
        private final AsynchronousConnection connection;
        private final ByteBuffer readBuffer;

        public ReadHandler(AsynchronousSocketChannel socket) {
            this.socket = socket;
            this.connection = new AsynchronousConnection(socket, encoder);
            this.readBuffer = ByteBuffer.allocate(maxPacketSize * 2);
        }

        @Override
        @SneakyThrows
        public void completed(Integer result, ByteBuffer buffer) {
            if (!running || result == -1) {
                socket.close();
                return;
            }
            buffer.flip();
            readBuffer.put(buffer);

            readBuffer.flip();
            while (true) {
                try {
                    PacketIn packet = decoder.decode(readBuffer);
                    if (packet == null)
                        break;
                    requestHandler.handle(packet, connection);
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
            log.error("Failed to read from client", exc);
            socket.close();
        }
    }
}
