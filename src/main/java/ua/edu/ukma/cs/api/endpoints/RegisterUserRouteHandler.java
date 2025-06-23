package ua.edu.ukma.cs.api.endpoints;

import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.api.routing.BaseRouteHandler;
import ua.edu.ukma.cs.api.routing.RouteHandlerResult;
import ua.edu.ukma.cs.request.RegisterUserRequestDto;
import ua.edu.ukma.cs.services.impl.UserService;

@RequiredArgsConstructor
public class RegisterUserRouteHandler extends BaseRouteHandler {
    private final UserService userService;
    private final RegisterUserRequestDto requestDto;

    @Override
    public RouteHandlerResult handle() throws Exception {
        return ok(userService.register(requestDto));
    }
}
