package ua.edu.ukma.cs.api;

import com.sun.net.httpserver.HttpServer;
import ua.edu.ukma.cs.api.routing.HttpMethod;
import ua.edu.ukma.cs.api.routing.Router;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ApiMain {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080), 10);
        Router router = buildRouter();
        server.createContext("/", router);
        server.start();
    }

    private static Router buildRouter() {
        Router router = new Router();

        router.addRoute("/login", HttpMethod.GET, routeContext -> {
            LoginRequest loginRequest = routeContext.objectMapper.readValue(routeContext.getBody(), LoginRequest.class);
            return new LoginRouteHandler(loginRequest);
        });

        router.addRoute("/user/(?<userId>\\d+)", HttpMethod.GET, routeContext -> {
            int id = Integer.parseInt(routeContext.getRouteParameters().get("userId"));
            return new GetUserRouteHandler(id);
        });

        return router;
    }
}
