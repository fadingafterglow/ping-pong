package ua.edu.ukma.cs.tcp.packets.payload;

import lombok.Data;

import java.util.UUID;

@Data
public class StartGameRequest {
    private final UUID gameLobbyId;
}
