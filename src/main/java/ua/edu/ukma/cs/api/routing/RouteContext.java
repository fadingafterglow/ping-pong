package ua.edu.ukma.cs.api.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.Map;

@RequiredArgsConstructor
public class RouteContext {
    @Getter
    private final Map<String, String> routeParameters;
    @Getter
    private final InputStream body;

    public final ObjectMapper objectMapper = new ObjectMapper();
}
