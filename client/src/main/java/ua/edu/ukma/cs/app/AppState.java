package ua.edu.ukma.cs.app;

import lombok.Getter;
import lombok.Setter;
import ua.edu.ukma.cs.connection.LobbyConnection;

@Getter
@Setter
public class AppState {

    private String jwtToken;

    private LobbyConnection lobbyConnection;
}
