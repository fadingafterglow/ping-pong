package ua.edu.ukma.cs.api.endpoints;

import ua.edu.ukma.cs.api.routing.BaseRouteHandler;
import ua.edu.ukma.cs.api.routing.RouteContext;
import ua.edu.ukma.cs.api.routing.RouteHandlerResult;
import ua.edu.ukma.cs.exception.ForbiddenException;
import ua.edu.ukma.cs.security.SecurityContext;
import ua.edu.ukma.cs.services.IGameService;

public class CreateLobbyRouteHandler extends BaseRouteHandler {
    private final IGameService gameService;

    public CreateLobbyRouteHandler(RouteContext routeContext, IGameService gameService) {
        super(routeContext);
        this.gameService = gameService;
    }

    @Override
    public RouteHandlerResult handle() throws Exception {
        SecurityContext securityContext = routeContext.getSecurityContext().orElseThrow(ForbiddenException::new);
        int userId = securityContext.getUserId();
        return RouteHandlerResult.string(gameService.createLobby(userId).toString());
    }
}
