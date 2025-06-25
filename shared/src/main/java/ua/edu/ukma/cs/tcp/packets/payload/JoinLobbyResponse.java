package ua.edu.ukma.cs.tcp.packets.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.edu.ukma.cs.game.lobby.GameLobbySnapshot;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
