package ua.edu.ukma.cs.app;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;
import ua.edu.ukma.cs.connection.LobbyConnection;

import java.util.UUID;

@Getter
public class AppState {

    private Integer userId;

    private String username;

    private String jwtToken;

    private UUID lobbyId;

    private LobbyConnection lobbyConnection;

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
        DecodedJWT decodedJWT = JWT.decode(jwtToken);
        this.userId = decodedJWT.getClaim("USER_ID").asInt();
        this.username = decodedJWT.getSubject();
    }

    public void setLobbyConnection(UUID lobbyId, LobbyConnection lobbyConnection) {
        this.lobbyId = lobbyId;
        if (this.lobbyConnection != null)
            this.lobbyConnection.disconnect();
        this.lobbyConnection = lobbyConnection;
    }
}
