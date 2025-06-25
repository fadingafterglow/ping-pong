package ua.edu.ukma.cs.tcp.packets.payload;

import lombok.Data;
import ua.edu.ukma.cs.game.lobby.GameLobbySnapshot;

@Data
public class JoinLobbyResponse {

    private boolean success;
    private String message;
    private GameLobbySnapshot lobby;

    public JoinLobbyResponse(String message) {
        this.message = message;
    }

    public JoinLobbyResponse(GameLobbySnapshot lobby) {
        this.success = true;
        this.message = "Joined";
        this.lobby = lobby;
    }
}
