package ua.edu.ukma.cs;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import ua.edu.ukma.cs.api.endpoints.LoginUserRouteHandler;
import ua.edu.ukma.cs.api.endpoints.RegisterUserRouteHandler;
import ua.edu.ukma.cs.api.filters.ExceptionHandlerFilter;
import ua.edu.ukma.cs.api.filters.JwtTokenFilter;
import ua.edu.ukma.cs.api.routing.HttpMethod;
import ua.edu.ukma.cs.api.routing.Router;
import ua.edu.ukma.cs.database.context.PersistenceContext;
import ua.edu.ukma.cs.database.migration.DefaultMigrationRunner;
import ua.edu.ukma.cs.request.LoginUserRequestDto;
import ua.edu.ukma.cs.request.RegisterUserRequestDto;
import ua.edu.ukma.cs.repository.UserRepository;
import ua.edu.ukma.cs.security.JwtServices;
import ua.edu.ukma.cs.service.UserService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

public class PingPongGame {

    private static final String PROPERTIES_FILE = "/db-connection.properties";

    public static void main(String[] args) throws IOException {
        System.out.println("Starting Ping Pong Game...");

        PersistenceContext.init(loadProperties());
        new DefaultMigrationRunner().runMigrations();

        HttpServer server = buildServer();
        server.start();
    }

    private static HttpServer buildServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080), 0);
        Router router = buildRouter();
        HttpContext httpContext = server.createContext("/", router);
        configureFilters(httpContext.getFilters());
        return server;
    }

    private static Router buildRouter() {
        Router router = new Router();

        router.addRoute("/register", HttpMethod.POST, routeContext -> {
            UserService userService = new UserService(new UserRepository());
            RegisterUserRequestDto registerUserRequest = routeContext.getJsonFromBody(RegisterUserRequestDto.class);
            return new RegisterUserRouteHandler(userService, registerUserRequest);
        });

        router.addRoute("/login", HttpMethod.POST, routeContext -> {
            UserService userService = new UserService(new UserRepository());
            LoginUserRequestDto loginUserRequest = routeContext.getJsonFromBody(LoginUserRequestDto.class);
            return new LoginUserRouteHandler(userService, loginUserRequest);
        });

        return router;
    }

    private static void configureFilters(List<Filter> filters) {
        filters.add(new ExceptionHandlerFilter());
        filters.add(new JwtTokenFilter(new JwtServices()));
    }

    @SneakyThrows
    private static Properties loadProperties() {
        Properties properties = new Properties();
        properties.load(PersistenceContext.class.getResourceAsStream(PROPERTIES_FILE));
        return properties;
    }
}
