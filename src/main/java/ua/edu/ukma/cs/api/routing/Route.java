package ua.edu.ukma.cs.api.routing;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {
    private final Pattern routePattern;
    private final HttpMethod httpMethod;

    @Getter
    private final IRouteHandlerFactory routeHandlerFactory;

    public Route(String routeRegex, HttpMethod httpMethod, IRouteHandlerFactory routeHandlerFactory) {
        this.routePattern = Pattern.compile(routeRegex);
        this.httpMethod = httpMethod;
        this.routeHandlerFactory = routeHandlerFactory;
    }

    public boolean matches(String concreteRoute, HttpMethod httpMethod) {
        if (this.httpMethod != httpMethod) {
            return false;
        }
        return routePattern.matcher(concreteRoute).matches();
    }

    public Map<String, String> getRouteParameters(String path) {
        Matcher matcher = routePattern.matcher(path);
        Map<String, Integer> namedGroupsIndexes = matcher.namedGroups();
        Map<String, String> result = new HashMap<>();
        if(matcher.find()) {
            for (var entry : namedGroupsIndexes.entrySet()) {
                String key = entry.getKey();
                String value = matcher.group(entry.getValue());
                result.put(key, value);
            }
        }
        return result;
    }
}
