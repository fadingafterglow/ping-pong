package ua.edu.ukma.cs.api.routing;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.SneakyThrows;
import ua.edu.ukma.cs.api.filters.JwtTokenFilter;
import ua.edu.ukma.cs.api.routing.exceptions.NoRouteMatchedException;
import ua.edu.ukma.cs.api.routing.exceptions.NotRegisteredHttpMethod;
import ua.edu.ukma.cs.api.routing.exceptions.RequestParsingErrorException;
import ua.edu.ukma.cs.security.SecurityContext;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Router implements HttpHandler {
    private final List<Route> routes = new ArrayList<>();

    public void addRoute(String route, HttpMethod httpMethod, IRouteHandlerFactory routeHandlerFactory) {
        routes.add(new Route(route, httpMethod, routeHandlerFactory));
    }

    @Override
    @SneakyThrows
    public void handle(HttpExchange exchange) {
        try {
            String path = getPath(exchange);
            Route route = matchRoute(exchange, path);

            Map<String, String> routeParameters = route.getRouteParameters(path);
            InputStream requestBody = exchange.getRequestBody();

            Object securityContext = exchange.getAttribute(JwtTokenFilter.SECURITY_CONTEXT_KEY);
            Optional<SecurityContext> optionalSecurityContext = securityContext == null
                    ? Optional.empty()
                    : Optional.of((SecurityContext) securityContext);

            RouteContext routeContext = new RouteContext(routeParameters, requestBody, optionalSecurityContext);
            BaseRouteHandler routeHandler = route.getRouteHandlerFactory().create(routeContext);
            RouteHandlerResult handlerResult = routeHandler.handle();

            exchange.sendResponseHeaders(handlerResult.statusCode(), handlerResult.body().length);
            exchange.getResponseBody().write(handlerResult.body());
        } catch (NoRouteMatchedException e) {
            exchange.sendResponseHeaders(404, 0);
        } catch (RequestParsingErrorException e) {
            exchange.sendResponseHeaders(400, 0);
        } catch (NotRegisteredHttpMethod e) {
            exchange.sendResponseHeaders(500, 0);
        } catch (Exception e) {
            throw e;
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
}
