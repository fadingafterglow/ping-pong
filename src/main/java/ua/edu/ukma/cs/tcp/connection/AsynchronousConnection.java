package ua.edu.ukma.cs.tcp.connection;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class AsynchronousConnection {

    private final AsynchronousSocketChannel socket;

    private final AtomicBoolean isWriting;
    private final ConcurrentLinkedQueue<ByteBuffer> writeQueue;

    public AsynchronousConnection(AsynchronousSocketChannel socket) {
        this.socket = socket;
        this.isWriting = new AtomicBoolean();
        this.writeQueue = new ConcurrentLinkedQueue<>();
    }

    public void enqueueWrite(ByteBuffer buffer) {
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
