package ua.edu.ukma.cs.api.endpoints;

import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.api.routing.BaseRouteHandler;
import ua.edu.ukma.cs.api.routing.RouteContext;
import ua.edu.ukma.cs.api.routing.RouteHandlerResult;
import ua.edu.ukma.cs.exception.ForbiddenException;
import ua.edu.ukma.cs.security.SecurityContext;
import ua.edu.ukma.cs.services.IGameResultService;

@RequiredArgsConstructor
public class GetAllCurrentUserGameResultsRouteHandler extends BaseRouteHandler {
    private final IGameResultService gameResultService;
    private final RouteContext routeContext;

    @Override
    public RouteHandlerResult handle() throws Exception {
        SecurityContext securityContext = routeContext.getSecurityContext().orElseThrow(ForbiddenException::new);
        return ok(gameResultService.getAllOfCurrentUser(securityContext));
    }
}
