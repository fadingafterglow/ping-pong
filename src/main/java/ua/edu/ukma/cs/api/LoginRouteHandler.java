package ua.edu.ukma.cs.api;

import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.api.routing.BaseRouteHandler;
import ua.edu.ukma.cs.api.routing.RouteHandlerResult;

@RequiredArgsConstructor
public class LoginRouteHandler extends BaseRouteHandler {
    private final LoginRequest loginRequest;

    @Override
    public RouteHandlerResult handle() throws Exception {
        return ok(loginRequest);
    }
}
