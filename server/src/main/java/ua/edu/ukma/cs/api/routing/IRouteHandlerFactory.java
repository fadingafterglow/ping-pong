package ua.edu.ukma.cs.api.routing;

public interface IRouteHandlerFactory {
    BaseRouteHandler create(RouteContext routeContext) throws Exception;
}
