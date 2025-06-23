package ua.edu.ukma.cs.api.routing;

import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.edu.ukma.cs.api.routing.exceptions.RequestParsingErrorException;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouterTest {
    private Router router;

    @BeforeEach
    void setUp() {
        router = new Router();
    }

    @Test
    void handle_whenRouteMatches_shouldRespond200() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestURI()).thenReturn(URI.create("/hello"));
        when(exchange.getRequestMethod()).thenReturn("GET");

        ByteArrayInputStream requestBody = new ByteArrayInputStream("{}".getBytes());
        ByteArrayOutputStream responseBody = new ByteArrayOutputStream();

        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(exchange.getResponseBody()).thenReturn(responseBody);
        String msg = "Hello";
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);

        router.addRoute("/hello", HttpMethod.GET, context -> new BaseRouteHandler() {
            @Override
            public RouteHandlerResult handle() {
                return new RouteHandlerResult(200, msgBytes);
            }
        });

        router.handle(exchange);

        verify(exchange).sendResponseHeaders(200, msgBytes.length);
        assertEquals(msg, responseBody.toString());
        verify(exchange).close();
    }

    @Test
    void handle_whenNoRouteMatches_shouldRespond404() throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestURI()).thenReturn(URI.create("/unknown"));
        when(exchange.getRequestMethod()).thenReturn("GET");
        String msg = "Hello";
        byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);

        router.addRoute("/hello", HttpMethod.GET, context -> new BaseRouteHandler() {
            @Override
            public RouteHandlerResult handle() {
                return new RouteHandlerResult(200, msgBytes);
            }
        });

        router.handle(exchange);

        verify(exchange).sendResponseHeaders(404, 0);
        verify(exchange).close();
    }

    @Test
    void handle_whenRequestParsingErrorExceptionThrown_shouldRespond400() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestURI()).thenReturn(URI.create("/hello"));
        when(exchange.getRequestMethod()).thenReturn("GET");

        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));

        router.addRoute("/hello", HttpMethod.GET, context -> new BaseRouteHandler() {
            @Override
            public RouteHandlerResult handle() throws Exception {
                throw new RequestParsingErrorException();
            }
        });

        router.handle(exchange);

        verify(exchange).sendResponseHeaders(400, 0);
        verify(exchange).close();
    }

    @Test
    void handle_whenUnexpectedExceptionThrown_shouldRespond500() throws Exception {
        HttpExchange exchange = mock(HttpExchange.class);
        when(exchange.getRequestURI()).thenReturn(URI.create("/hello"));
        when(exchange.getRequestMethod()).thenReturn("GET");

        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes()));

        router.addRoute("/hello", HttpMethod.GET, context -> new BaseRouteHandler() {
            @Override
            public RouteHandlerResult handle() {
                throw new RuntimeException("Boom");
            }
        });

        router.handle(exchange);

        verify(exchange).sendResponseHeaders(500, 0);
        verify(exchange).close();
    }
}
