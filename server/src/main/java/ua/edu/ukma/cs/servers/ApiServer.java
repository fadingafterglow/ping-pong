package ua.edu.ukma.cs.servers;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpServer;
import ua.edu.ukma.cs.api.routing.Router;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

public class ApiServer implements IServer {

    private final int port;
    private final Router router;
    private final List<Filter> filters;

    private boolean running;

    private HttpServer server;

    public ApiServer(Router router, List<Filter> filters, Properties properties) {
        this.port = Integer.parseInt(properties.getProperty("api.server.port", "8080"));
        this.router = router;
        this.filters = filters;
    }

    @Override
    public void start() throws IOException {
        if (running) return;
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.createContext("/", router)
                .getFilters().addAll(filters);
        server.start();
        running = true;
    }

    @Override
    public void stop() {
        if (!running) return;
        server.stop(3);
        running = false;
    }
}
