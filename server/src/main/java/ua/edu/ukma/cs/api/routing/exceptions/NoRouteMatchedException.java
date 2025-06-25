package ua.edu.ukma.cs.api.routing.exceptions;

public class NoRouteMatchedException extends Exception {
    public NoRouteMatchedException(String path) {
        super("No route was matched for path: " + path);
    }
}
