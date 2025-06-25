package ua.edu.ukma.cs.tcp.packets.payload;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinLobbyRequest {
    private UUID gameLobbyId;
    private String userJwt;
    private byte[] symmetricKey;
}
