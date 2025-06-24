package ua.edu.ukma.cs.game.lobby;

import lombok.Builder;
import ua.edu.ukma.cs.game.configuration.GameConfiguration;

@Builder
public record GameLobbySnapshot(
    GameLobbyState state,
    int creatorId,
    Integer otherPlayerId,
    GameConfiguration gameConfiguration
)
{}
