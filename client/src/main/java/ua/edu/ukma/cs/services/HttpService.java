package ua.edu.ukma.cs.services;

import lombok.SneakyThrows;
import ua.edu.ukma.cs.app.AppState;
import ua.edu.ukma.cs.utils.ObjectMapperHolder;

import javax.net.ssl.SSLContext;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;

public class HttpService {

    private final String baseUrl;
    private final HttpClient client;
    private final AppState appState;

    public HttpService(AppState appState, SSLContext sslContext, Properties properties) {
        this.baseUrl = properties.getProperty("api.server.url");
        this.client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();
        this.appState = appState;
    }

    @SneakyThrows
    public HttpResponse<byte[]> get(String path) {
        return get(path, Optional.empty());
    }

    @SneakyThrows
    public HttpResponse<byte[]> get(String path, Object body) {
        byte[] jsonBody = ObjectMapperHolder.get().writeValueAsBytes(body);
        return get(path, Optional.of(jsonBody));
    }

    @SneakyThrows
    private HttpResponse<byte[]> get(String path, Optional<byte[]> jsonBody) {
        HttpRequest.Builder requestBuilder = buildBaseRequest(path)
                .GET();
        jsonBody.ifPresent(b -> requestBuilder
                .method("GET", HttpRequest.BodyPublishers.ofByteArray(b))
                .header("Content-Type", "application/json")
        );
        return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    public HttpResponse<byte[]> post(String path) {
        return post(path, Optional.empty());
    }

    @SneakyThrows
    public HttpResponse<byte[]> post(String path, Object body) {
        byte[] jsonBody = ObjectMapperHolder.get().writeValueAsBytes(body);
        return post(path, Optional.of(jsonBody));
    }

    @SneakyThrows
    private HttpResponse<byte[]> post(String path, Optional<byte[]> jsonBody) {
        HttpRequest.Builder requestBuilder = buildBaseRequest(path)
                .header("Content-Type", "application/json")
                .POST(jsonBody.map(HttpRequest.BodyPublishers::ofByteArray).orElseGet(HttpRequest.BodyPublishers::noBody));

        HttpRequest request = requestBuilder.build();
        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    private HttpRequest.Builder buildBaseRequest(String path) {
        var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path));

        String jwtToken = appState.getJwtToken();
        if (jwtToken != null) {
            requestBuilder.header("Authentication", jwtToken);
        }

        return requestBuilder;
    }
}