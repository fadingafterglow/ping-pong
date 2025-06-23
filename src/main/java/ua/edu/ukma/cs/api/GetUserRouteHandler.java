package ua.edu.ukma.cs.api;

import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.api.routing.BaseRouteHandler;
import ua.edu.ukma.cs.api.routing.RouteHandlerResult;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class GetUserRouteHandler extends BaseRouteHandler {
    private final int id;

    private final Map<Integer, String> users = new HashMap<>();

    {
     users.put(1, "qwerty");
    }

    @Override
    public RouteHandlerResult handle() throws Exception {
        String user = users.get(id);
        if(user == null) {
            return notFound("User with id " + id + " was not found");
        }
        return ok(user);
    }
}
