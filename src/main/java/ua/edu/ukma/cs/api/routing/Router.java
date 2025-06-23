package ua.edu.ukma.cs.api.routing;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ua.edu.ukma.cs.api.routing.exceptions.NoRouteMatchedException;
import ua.edu.ukma.cs.api.routing.exceptions.NotRegisteredHttpMethod;
import ua.edu.ukma.cs.api.routing.exceptions.RequestParsingErrorException;
import ua.edu.ukma.cs.exception.ForbiddenException;
import ua.edu.ukma.cs.exception.NotFoundException;
import ua.edu.ukma.cs.exception.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Router implements HttpHandler {
    private final List<Route> routes = new ArrayList<>();

    public void addRoute(String route, HttpMethod httpMethod, IRouteHandlerFactory routeHandlerFactory) {
        routes.add(new Route(route, httpMethod, routeHandlerFactory));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = getPath(exchange);
            Route route = matchRoute(exchange, path);

            Map<String, String> routeParameters = route.getRouteParameters(path);
            InputStream requestBody = exchange.getRequestBody();

            RouteHandlerResult handlerResult = callRouteHandler(routeParameters, requestBody, route);

            exchange.sendResponseHeaders(handlerResult.statusCode(), handlerResult.body().length);
            exchange.getResponseBody().write(handlerResult.body());
        } catch (NoRouteMatchedException | NotFoundException e) {
            exchange.sendResponseHeaders(404, 0);
        } catch (ForbiddenException e) {
            exchange.sendResponseHeaders(403, 0);
        } catch (RequestParsingErrorException | ValidationException e) {
            exchange.sendResponseHeaders(400, 0);
        } catch (Exception e) {
            exchange.sendResponseHeaders(500, 0);
        } finally {
            exchange.close();
        }
    }

    private String getPath(HttpExchange exchange) {
        return exchange.getRequestURI().getPath();
    }

    private Route matchRoute(HttpExchange exchange, String path) throws NoRouteMatchedException, NotRegisteredHttpMethod {
        String httpMethodStr = exchange.getRequestMethod();
        try {
            HttpMethod httpMethod = HttpMethod.valueOf(httpMethodStr);
            return routes.stream()
                    .filter(r -> r.matches(path, httpMethod))
                    .findFirst()
                    .orElseThrow(() -> new NoRouteMatchedException(path));
        } catch (IllegalArgumentException e) {
            throw new NotRegisteredHttpMethod(httpMethodStr);
        }
    }

    private RouteHandlerResult callRouteHandler(Map<String, String> routeParameters, InputStream requestBody, Route route) throws Exception {
        RouteContext routeContext = new RouteContext(routeParameters, requestBody);
        BaseRouteHandler routeHandler = route.getRouteHandlerFactory().create(routeContext);
        return routeHandler.handle();
    }
}
