package ua.edu.ukma.cs.api.routing;

public abstract class BaseRouteHandler {
    protected final RouteContext routeContext;

    protected BaseRouteHandler(RouteContext routeContext) {
        this.routeContext = routeContext;
    }

    public abstract RouteHandlerResult handle() throws Exception;
}
