package ua.edu.ukma.cs.tcp.connection;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ua.edu.ukma.cs.tcp.encoders.IEncoder;
import ua.edu.ukma.cs.tcp.packets.PacketOut;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AsynchronousConnection {

    private final AsynchronousSocketChannel socket;
    private final IEncoder<PacketOut> encoder;

    private final AtomicBoolean isWriting;
    private final ConcurrentLinkedQueue<ByteBuffer> writeQueue;
    private final ConcurrentHashMap<String, Object> attributes;

    public AsynchronousConnection(AsynchronousSocketChannel socket, IEncoder<PacketOut> encoder) {
        this.socket = socket;
        this.encoder = encoder;
        this.isWriting = new AtomicBoolean();
        this.writeQueue = new ConcurrentLinkedQueue<>();
        this.attributes = new ConcurrentHashMap<>();
    }

    public boolean isClosed() {
        return !socket.isOpen();
    }

    public void setAttribute(String name, Object value) {
        if (value == null)
            attributes.remove(name);
        else
            attributes.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
    }

    public void enqueueWrite(PacketOut packet) {
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
            log.error("Failed to write to client", exc);
            socket.close();
        }
    }
}
