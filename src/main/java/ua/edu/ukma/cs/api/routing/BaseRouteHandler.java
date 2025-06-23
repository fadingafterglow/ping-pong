package ua.edu.ukma.cs.api.routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

public abstract class BaseRouteHandler {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    public abstract RouteHandlerResult handle() throws Exception;

    protected RouteHandlerResult ok(Object body) throws JsonProcessingException {
        return new RouteHandlerResult(200, objectMapper.writeValueAsBytes(body));
    }

    protected RouteHandlerResult notFound(String msg) {
        return new RouteHandlerResult(404, msg.getBytes(StandardCharsets.UTF_8));
    }
}
