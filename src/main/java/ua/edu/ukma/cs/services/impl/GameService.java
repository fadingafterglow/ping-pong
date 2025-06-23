package ua.edu.ukma.cs.services.impl;

import ua.edu.ukma.cs.services.IGameService;
import ua.edu.ukma.cs.tcp.handlers.ITcpRequestHandler;
import ua.edu.ukma.cs.tcp.packets.PacketIn;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.UUID;

public class GameService implements IGameService, ITcpRequestHandler {

    @Override
    public UUID createLobby(int creatorId) {
        return null;
    }

    @Override
    public void handle(PacketIn packet, AsynchronousSocketChannel socket) {

    }
}
