package ua.edu.ukma.cs.services;

import ua.edu.ukma.cs.api.request.GameResultFilterDto;
import ua.edu.ukma.cs.api.response.GameResultListResponse;
import ua.edu.ukma.cs.api.response.GameResultResponse;
import ua.edu.ukma.cs.security.SecurityContext;

public interface IGameResultService {

    GameResultResponse getById(int id, SecurityContext securityContext);

    GameResultListResponse getCurrentUserGameResults(SecurityContext securityContext, GameResultFilterDto filterDto);
}
