package ua.edu.ukma.cs;

import com.sun.net.httpserver.Filter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ua.edu.ukma.cs.api.endpoints.LoginUserRouteHandler;
import ua.edu.ukma.cs.api.endpoints.RegisterUserRouteHandler;
import ua.edu.ukma.cs.api.filters.ExceptionHandlerFilter;
import ua.edu.ukma.cs.api.filters.JwtTokenFilter;
import ua.edu.ukma.cs.api.routing.HttpMethod;
import ua.edu.ukma.cs.api.routing.Router;
import ua.edu.ukma.cs.database.context.PersistenceContext;
import ua.edu.ukma.cs.database.migration.DefaultMigrationRunner;
import ua.edu.ukma.cs.api.request.LoginUserRequestDto;
import ua.edu.ukma.cs.api.request.RegisterUserRequestDto;
import ua.edu.ukma.cs.repository.UserRepository;
import ua.edu.ukma.cs.security.JwtServices;
import ua.edu.ukma.cs.servers.ApiServer;
import ua.edu.ukma.cs.servers.IServer;
import ua.edu.ukma.cs.services.impl.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
public class PingPongGame {

    private static final String PROPERTIES_FILE = "/application.properties";

    public static void main(String[] args) throws IOException {
        log.info("Starting Ping Pong Game...");

        Properties properties = loadProperties();
        PersistenceContext.init(properties);
        new DefaultMigrationRunner().runMigrations();

        JwtServices jwtServices = new JwtServices(properties);
        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository, jwtServices);

        IServer apiServer = new ApiServer(buildRouter(userService), buildFilters(jwtServices), properties);
        apiServer.start();
    }

    private static Router buildRouter(UserService userService) {
        Router router = new Router();

        router.addRoute("/register", HttpMethod.POST, routeContext -> {
            RegisterUserRequestDto registerUserRequest = routeContext.getJsonFromBody(RegisterUserRequestDto.class);
            return new RegisterUserRouteHandler(userService, registerUserRequest);
        });

        router.addRoute("/login", HttpMethod.POST, routeContext -> {
            LoginUserRequestDto loginUserRequest = routeContext.getJsonFromBody(LoginUserRequestDto.class);
            return new LoginUserRouteHandler(userService, loginUserRequest);
        });

        return router;
    }

    private static List<Filter> buildFilters(JwtServices jwtServices) {
        List<Filter> filters = new ArrayList<>();
        filters.add(new ExceptionHandlerFilter());
        filters.add(new JwtTokenFilter(jwtServices));
        return filters;
    }

    @SneakyThrows
    private static Properties loadProperties() {
        Properties properties = new Properties();
        properties.load(PersistenceContext.class.getResourceAsStream(PROPERTIES_FILE));
        return properties;
    }
}
