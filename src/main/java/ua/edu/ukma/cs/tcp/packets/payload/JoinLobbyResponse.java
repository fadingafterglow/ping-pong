package ua.edu.ukma.cs.tcp.packets.payload;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ua.edu.ukma.cs.game.lobby.GameLobbySnapshot;

@Data
@EqualsAndHashCode(callSuper = true)
public class JoinLobbyResponse extends CommonResponse {
    private GameLobbySnapshot lobby;

    public JoinLobbyResponse(String message) {
        super(false, message);
    }

    public JoinLobbyResponse(GameLobbySnapshot lobby) {
        super(true, "Joined");
        this.lobby = lobby;
    }
}
