package ua.edu.ukma.cs.api.endpoints;

import ua.edu.ukma.cs.api.routing.BaseRouteHandler;
import ua.edu.ukma.cs.api.routing.RouteContext;
import ua.edu.ukma.cs.api.routing.RouteHandlerResult;
import ua.edu.ukma.cs.api.request.LoginUserRequestDto;
import ua.edu.ukma.cs.services.impl.UserService;

public class LoginUserRouteHandler extends BaseRouteHandler {
    private final UserService userService;

    public LoginUserRouteHandler(RouteContext routeContext, UserService userService) {
        super(routeContext);
        this.userService = userService;
    }

    @Override
    public RouteHandlerResult handle() throws Exception {
        LoginUserRequestDto dto = routeContext.getJsonFromBody(LoginUserRequestDto.class);
        return RouteHandlerResult.string(userService.login(dto));
    }
}
