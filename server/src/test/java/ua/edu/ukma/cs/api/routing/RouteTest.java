package ua.edu.ukma.cs.api.routing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class RouteTest {
    @Test
    public void matches_givenNotMatchableHttpMethod_shouldReturnFalse() {
        Route route = new Route("user", HttpMethod.GET, mockRouteHandlerFactory());

        assertFalse(route.matches("user", HttpMethod.POST));
    }

    @ParameterizedTest
    @MethodSource("provideArguments_matches")
    public void matches(boolean expectedResult, String routePattern, String concretePath) {
        HttpMethod httpMethod = HttpMethod.GET;
        Route route = new Route(routePattern, httpMethod, mockRouteHandlerFactory());

        assertEquals(expectedResult, route.matches(concretePath, httpMethod));
    }

    public static Stream<Arguments> provideArguments_matches() {
        return Stream.of(
                Arguments.of(true, "user", "user"),
                Arguments.of(false, "user", "u"),
                Arguments.of(false, "user", ""),
                Arguments.of(false, "user", "user/abc"),

                Arguments.of(true, "user/qwerty", "user/qwerty"),
                Arguments.of(false, "user/qwerty", "user/qwerty/abc"),
                Arguments.of(false, "user/qwerty", "user"),
                Arguments.of(false, "user/qwerty", "user/"),
                Arguments.of(false, "user/qwerty", "user/qwert"),

                Arguments.of(true, "user/(?<userId>\\d+)", "user/123"),
                Arguments.of(false, "user/(?<userId>\\d+)", "user/"),
                Arguments.of(false, "user/(?<userId>\\d+)", "user"),
                Arguments.of(false, "user/(?<userId>\\d+)", "user/abc"),
                Arguments.of(false, "user/(?<userId>\\d+)", "user/123abc"),
                Arguments.of(false, "user/(?<userId>\\d+)", "user/abc123"),
                Arguments.of(false, "user/(?<userId>\\d+)", "user/123/abc")
        );
    }

    @Test
    public void getRouteParameters_givenPathThatMatchesPattern_shouldReturnAllParameters() {
        String pattern = "user/(?<id>\\d+)/(?<name>[A-Za-z]+)/abc/(?<surname>[A-Za-z]+)";
        Route route = new Route(pattern, HttpMethod.GET, mockRouteHandlerFactory());
        String expectedId = "123";
        String expectedName = "john";
        String expectedSurname = "doe";
        String path = String.format("user/%s/%s/abc/%s", expectedId, expectedName, expectedSurname);

        Map<String, String> routeParameters = route.getRouteParameters(path);

        assertEquals(3, routeParameters.size());
        assertEquals(expectedId, routeParameters.get("id"));
        assertEquals(expectedName, routeParameters.get("name"));
        assertEquals(expectedSurname, routeParameters.get("surname"));
    }

    @Test
    public void getRouteParameters_givenPathThatNotMatchesPattern_shouldReturnEmptyMap() {
        String pattern = "user/(?<id>\\d+)/(?<name>[A-Za-z]+)/abc/(?<surname>[A-Za-z]+)";
        Route route = new Route(pattern, HttpMethod.GET, mockRouteHandlerFactory());
        String path = "qwerty/abc";

        Map<String, String> routeParameters = route.getRouteParameters(path);

        assertEquals(0, routeParameters.size());
    }

    @Test
    public void getRouteParameters_givenPathThatPartiallyMatchesPattern_shouldReturnEmptyMap() {
        String pattern = "user/(?<id>\\d+)/(?<name>[A-Za-z]+)/abc/(?<surname>[A-Za-z]+)";
        Route route = new Route(pattern, HttpMethod.GET, mockRouteHandlerFactory());
        String expectedId = "123";
        String expectedName = "john";
        String path = String.format("user/%s/%s/abc", expectedId, expectedName);

        Map<String, String> routeParameters = route.getRouteParameters(path);

        assertEquals(0, routeParameters.size());
    }

    private static IRouteHandlerFactory mockRouteHandlerFactory() {
        return routeContext -> new BaseRouteHandler(routeContext) {
            @Override
            public RouteHandlerResult handle() {
                throw new RuntimeException("Not implemented");
            }
        };
    }
}