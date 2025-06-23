package ua.edu.ukma.cs.tcp.decoders;

import java.nio.ByteBuffer;

public interface IDecoder<T> {

    T decode(ByteBuffer buffer);
}
