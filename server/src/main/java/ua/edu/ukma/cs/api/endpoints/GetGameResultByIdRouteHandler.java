package ua.edu.ukma.cs.api.endpoints;

import ua.edu.ukma.cs.api.routing.BaseRouteHandler;
import ua.edu.ukma.cs.api.routing.RouteContext;
import ua.edu.ukma.cs.api.routing.RouteHandlerResult;
import ua.edu.ukma.cs.exception.ForbiddenException;
import ua.edu.ukma.cs.security.SecurityContext;
import ua.edu.ukma.cs.services.IGameResultService;

public class GetGameResultByIdRouteHandler extends BaseRouteHandler {
    private final IGameResultService gameResultService;

    public GetGameResultByIdRouteHandler(RouteContext routeContext, IGameResultService gameResultService) {
        super(routeContext);
        this.gameResultService = gameResultService;
    }

    @Override
    public RouteHandlerResult handle() throws Exception {
        int id = routeContext.getIntFromRouteParam("id");
        SecurityContext securityContext = routeContext.getSecurityContext().orElseThrow(ForbiddenException::new);
        return RouteHandlerResult.json(gameResultService.getById(id, securityContext));
    }
}
