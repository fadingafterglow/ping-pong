package ua.edu.ukma.cs.api.endpoints;

import ua.edu.ukma.cs.api.routing.BaseRouteHandler;
import ua.edu.ukma.cs.api.routing.RouteContext;
import ua.edu.ukma.cs.api.routing.RouteHandlerResult;
import ua.edu.ukma.cs.api.request.RegisterUserRequestDto;
import ua.edu.ukma.cs.services.impl.UserService;

public class RegisterUserRouteHandler extends BaseRouteHandler {
    private final UserService userService;

    public RegisterUserRouteHandler(RouteContext routeContext, UserService userService) {
        super(routeContext);
        this.userService = userService;
    }

    @Override
    public RouteHandlerResult handle() throws Exception {
        RegisterUserRequestDto requestDto = routeContext.getJsonFromBody(RegisterUserRequestDto.class);
        return RouteHandlerResult.json(userService.register(requestDto));
    }
}
