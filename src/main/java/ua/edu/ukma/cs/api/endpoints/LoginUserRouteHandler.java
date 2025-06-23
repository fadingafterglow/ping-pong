package ua.edu.ukma.cs.api.endpoints;

import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.api.routing.BaseRouteHandler;
import ua.edu.ukma.cs.api.routing.RouteHandlerResult;
import ua.edu.ukma.cs.request.LoginUserRequestDto;
import ua.edu.ukma.cs.services.impl.UserService;

@RequiredArgsConstructor
public class LoginUserRouteHandler extends BaseRouteHandler {
    private final UserService userService;
    private final LoginUserRequestDto loginUserRequestDto;

    @Override
    public RouteHandlerResult handle() throws Exception {
        return ok(userService.login(loginUserRequestDto));
    }
}
