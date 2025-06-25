package ua.edu.ukma.cs.api.routing;

import com.fasterxml.jackson.core.JsonProcessingException;
import ua.edu.ukma.cs.utils.SharedObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public record RouteHandlerResult(int statusCode, byte[] body, Optional<String> contentType) {
    public RouteHandlerResult(int statusCode, byte[] body) {
        this(statusCode, body, Optional.empty());
    }

    public static RouteHandlerResult json(Object body) throws JsonProcessingException {
        return new RouteHandlerResult(200, SharedObjectMapper.S.writeValueAsBytes(body), Optional.of("application/json"));
    }

    public static RouteHandlerResult string(String str) {
        return new RouteHandlerResult(200, str.getBytes(StandardCharsets.UTF_8), Optional.of("text/plain"));
    }

    public static RouteHandlerResult bytes(byte[] bytes) {
        return new RouteHandlerResult(200, bytes, Optional.of("application/octet-stream"));
    }
}
