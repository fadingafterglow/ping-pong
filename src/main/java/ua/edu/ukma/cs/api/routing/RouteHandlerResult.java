package ua.edu.ukma.cs.api.routing;

import lombok.Getter;

@Getter
public class RouteHandlerResult {
    private final int statusCode;
    private final byte[] body;

    public RouteHandlerResult(int statusCode, byte[] body) {
        this.statusCode = statusCode;
        this.body = body;
    }
}
