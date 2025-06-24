package ua.edu.ukma.cs.services;

import ua.edu.ukma.cs.api.response.GameResultResponse;
import ua.edu.ukma.cs.security.SecurityContext;

import java.util.List;

public interface IGameResultService {

    GameResultResponse getById(int id, SecurityContext securityContext);

    List<GameResultResponse> getAllOfCurrentUser(SecurityContext securityContext);
}
