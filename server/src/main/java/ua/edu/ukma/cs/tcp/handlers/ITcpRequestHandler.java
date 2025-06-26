package ua.edu.ukma.cs.tcp.handlers;

import ua.edu.ukma.cs.tcp.connection.AsynchronousConnection;
import ua.edu.ukma.cs.tcp.packets.PacketIn;

public interface ITcpRequestHandler {

    void handle(PacketIn packet, AsynchronousConnection connection);

    void handleDisconnect(AsynchronousConnection connection);
}
