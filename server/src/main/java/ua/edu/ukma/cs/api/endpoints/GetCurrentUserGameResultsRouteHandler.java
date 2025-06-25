package ua.edu.ukma.cs.api.endpoints;

import ua.edu.ukma.cs.api.request.GameResultFilterDto;
import ua.edu.ukma.cs.api.routing.BaseRouteHandler;
import ua.edu.ukma.cs.api.routing.RouteContext;
import ua.edu.ukma.cs.api.routing.RouteHandlerResult;
import ua.edu.ukma.cs.exception.ForbiddenException;
import ua.edu.ukma.cs.security.SecurityContext;
import ua.edu.ukma.cs.services.IGameResultService;

public class GetCurrentUserGameResultsRouteHandler extends BaseRouteHandler {
    private final IGameResultService gameResultService;

    public GetCurrentUserGameResultsRouteHandler(RouteContext routeContext, IGameResultService gameResultService) {
        super(routeContext);
        this.gameResultService = gameResultService;
    }

    @Override
    public RouteHandlerResult handle() throws Exception {
        SecurityContext securityContext = routeContext.getSecurityContext().orElseThrow(ForbiddenException::new);
        GameResultFilterDto filter = routeContext.getOptionalJsonFromBody(GameResultFilterDto.class)
                .orElse(GameResultFilterDto.builder().build());
        return RouteHandlerResult.json(gameResultService.getCurrentUserGameResults(securityContext, filter));
    }
}
