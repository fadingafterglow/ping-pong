package ua.edu.ukma.cs;

import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import ua.edu.ukma.cs.api.CreateUserRouteHandler;
import ua.edu.ukma.cs.api.routing.HttpMethod;
import ua.edu.ukma.cs.api.routing.Router;
import ua.edu.ukma.cs.database.context.PersistenceContext;
import ua.edu.ukma.cs.database.migration.DefaultMigrationRunner;
import ua.edu.ukma.cs.dto.CreateUserRequestDto;
import ua.edu.ukma.cs.repository.UserRepository;
import ua.edu.ukma.cs.service.UserService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Properties;

public class PingPongGame {

    private static final String PROPERTIES_FILE = "/db-connection.properties";

    public static void main(String[] args) throws IOException {
        System.out.println("Starting Ping Pong Game...");

        PersistenceContext.init(loadProperties());
        new DefaultMigrationRunner().runMigrations();

        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 8080), 0);
        Router router = buildRouter();
        server.createContext("/", router);
        server.start();
    }

    private static Router buildRouter() {
        Router router = new Router();

        router.addRoute("/user", HttpMethod.POST, routeContext -> {
            UserService userService = new UserService(new UserRepository());
            CreateUserRequestDto createUserRequest = routeContext.getJsonFromBody(CreateUserRequestDto.class);
            return new CreateUserRouteHandler(userService, createUserRequest);
        });

        return router;
    }

    @SneakyThrows
    private static Properties loadProperties() {
        Properties properties = new Properties();
        properties.load(PersistenceContext.class.getResourceAsStream(PROPERTIES_FILE));
        return properties;
    }
}
