package ua.edu.ukma.cs;

import com.sun.net.httpserver.Filter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ua.edu.ukma.cs.api.endpoints.*;
import ua.edu.ukma.cs.api.filters.ExceptionHandlerFilter;
import ua.edu.ukma.cs.api.filters.JwtTokenFilter;
import ua.edu.ukma.cs.api.routing.HttpMethod;
import ua.edu.ukma.cs.api.routing.Router;
import ua.edu.ukma.cs.database.context.PersistenceContext;
import ua.edu.ukma.cs.database.migration.DefaultMigrationRunner;
import ua.edu.ukma.cs.encryption.AesEncryptionService;
import ua.edu.ukma.cs.repository.GameResultRepository;
import ua.edu.ukma.cs.repository.UserRepository;
import ua.edu.ukma.cs.security.JwtServices;
import ua.edu.ukma.cs.servers.ApiServer;
import ua.edu.ukma.cs.servers.GameServer;
import ua.edu.ukma.cs.servers.IServer;
import ua.edu.ukma.cs.services.IAsymmetricDecryptionService;
import ua.edu.ukma.cs.services.IGameResultService;
import ua.edu.ukma.cs.services.IGameService;
import ua.edu.ukma.cs.encryption.ISymmetricEncryptionService;
import ua.edu.ukma.cs.services.impl.*;
import ua.edu.ukma.cs.tcp.decoders.IDecoder;
import ua.edu.ukma.cs.tcp.decoders.PacketDecoder;
import ua.edu.ukma.cs.tcp.encoders.IEncoder;
import ua.edu.ukma.cs.tcp.encoders.PacketEncoder;
import ua.edu.ukma.cs.tcp.packets.PacketIn;
import ua.edu.ukma.cs.tcp.packets.PacketOut;

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

        GameResultRepository gameResultRepository = new GameResultRepository();
        IGameResultService gameResultService = new GameResultService(gameResultRepository);

        IAsymmetricDecryptionService asymmetricEncryptionService = new RsaDecryptionService();
        ISymmetricEncryptionService symmetricEncryptionService = new AesEncryptionService();
        GameService gameService = new GameService(gameResultService, jwtServices, asymmetricEncryptionService, symmetricEncryptionService, properties);

        IServer apiServer = new ApiServer(
                buildRouter(userService, gameResultService, gameService, asymmetricEncryptionService),
                buildFilters(jwtServices),
                properties
        );

        IEncoder<PacketOut> encoder = new PacketEncoder();
        IDecoder<PacketIn> decoder = new PacketDecoder();
        IServer gameServer = new GameServer(decoder, encoder, gameService, properties);

        apiServer.start();
        gameServer.start();
    }

    private static Router buildRouter(UserService userService,
                                      IGameResultService gameResultService,
                                      IGameService gameService,
                                      IAsymmetricDecryptionService asymmetricEncryptionService) {
        Router router = new Router();

        router.addRoute("/register",
                HttpMethod.POST,
                routeContext -> new RegisterUserRouteHandler(routeContext, userService)
        );

        router.addRoute("/login",
                HttpMethod.POST,
                routeContext -> new LoginUserRouteHandler(routeContext, userService)
        );

        router.addRoute("/game-result",
                HttpMethod.GET,
                routeContext -> new GetCurrentUserGameResultsRouteHandler(routeContext, gameResultService)
        );

        router.addRoute("/game-result/(?<id>\\d+)",
                HttpMethod.GET,
                routeContext -> new GetGameResultByIdRouteHandler(routeContext, gameResultService)
        );

        router.addRoute("/game",
                HttpMethod.POST,
                routeContext -> new CreateLobbyRouteHandler(routeContext, gameService)
        );

        router.addRoute("/public-key",
                HttpMethod.GET,
                routeContext -> new GetPublicKeyRouteHandler(routeContext, asymmetricEncryptionService)
        );

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
