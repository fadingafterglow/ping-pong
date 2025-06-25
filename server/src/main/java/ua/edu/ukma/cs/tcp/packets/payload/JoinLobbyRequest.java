package ua.edu.ukma.cs.tcp.packets.payload;

import lombok.*;

import java.util.UUID;

@Data
public class JoinLobbyRequest {
    private final UUID gameLobbyId;
    private final String userJwt;
    private final byte[] symmetricKey;
}
