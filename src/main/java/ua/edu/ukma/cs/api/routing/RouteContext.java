package ua.edu.ukma.cs.api.routing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.api.routing.exceptions.RequestParsingErrorException;
import ua.edu.ukma.cs.security.SecurityContext;
import ua.edu.ukma.cs.util.SharedObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class RouteContext {
    private final Map<String, String> routeParameters;
    private final InputStream body;

    @Getter
    private final Optional<SecurityContext> securityContext;

    public <T> T getJsonFromBody(Class<T> clazz) throws RequestParsingErrorException {
        try {
            return SharedObjectMapper.S.readValue(body, clazz);
        } catch (IOException e) {
            throw new RequestParsingErrorException();
        }
    }

    public int getIntFromRouteParam(String paramName) throws RequestParsingErrorException {
        try {
            return Integer.parseInt(routeParameters.get(paramName));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new RequestParsingErrorException();
        }
    }

    public String getStringFromRouteParam(String paramName) throws RequestParsingErrorException {
        String value = routeParameters.get(paramName);
        if(value == null) {
            throw new RequestParsingErrorException();
        }
        return value;
    }
}
