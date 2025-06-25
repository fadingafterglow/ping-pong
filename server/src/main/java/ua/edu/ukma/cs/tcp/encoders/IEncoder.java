package ua.edu.ukma.cs.tcp.encoders;

import java.nio.ByteBuffer;

public interface IEncoder<T> {

    ByteBuffer encode(T object);
}
