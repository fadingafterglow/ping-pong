package ua.edu.ukma.cs.api.routing;

import java.io.InputStream;
import java.util.Map;

public record RouteContext(Map<String, String> routeParameters, InputStream body) {
}
