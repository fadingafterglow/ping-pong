package ua.edu.ukma.cs.tcp.handlers;

import ua.edu.ukma.cs.tcp.packets.PacketIn;

import java.nio.channels.AsynchronousSocketChannel;

public interface ITcpRequestHandler {

    void handle(PacketIn packet, AsynchronousSocketChannel socket);
}
