package ua.edu.ukma.cs.api;

import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.api.routing.BaseRouteHandler;
import ua.edu.ukma.cs.api.routing.RouteHandlerResult;
import ua.edu.ukma.cs.dto.CreateUserRequestDto;
import ua.edu.ukma.cs.service.UserService;

@RequiredArgsConstructor
public class CreateUserRouteHandler extends BaseRouteHandler {
    private final UserService userService;
    private final CreateUserRequestDto requestDto;

    @Override
    public RouteHandlerResult handle() throws Exception {
        return ok(userService.create(requestDto));
    }
}
