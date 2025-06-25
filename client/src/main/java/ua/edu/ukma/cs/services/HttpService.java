package ua.edu.ukma.cs.services;

import lombok.SneakyThrows;
import ua.edu.ukma.cs.app.AppState;
import ua.edu.ukma.cs.utils.ObjectMapperHolder;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class HttpService {
    private static final String BASE_URL = "http://localhost:8080";
    private final HttpClient client;

    public HttpService() {
        this.client = HttpClient.newHttpClient();
    }

    @SneakyThrows
    public HttpResponse<byte[]> get(String path) {
        HttpRequest request = buildBaseRequest(path)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    public HttpResponse<byte[]> post(String path) {
        return post(path, Optional.empty());
    }

    @SneakyThrows
    public HttpResponse<byte[]> post(String path, Object body) {
        String jsonBody = ObjectMapperHolder.get().writeValueAsString(body);
        return post(path, Optional.of(jsonBody));
    }

    @SneakyThrows
    private HttpResponse<byte[]> post(String path, Optional<String> jsonBody) {
        HttpRequest.Builder requestBuilder = buildBaseRequest(path)
                .header("Content-Type", "application/json")
                .POST(jsonBody.map(s -> HttpRequest.BodyPublishers.ofString(s, StandardCharsets.UTF_8)).orElseGet(HttpRequest.BodyPublishers::noBody));

        HttpRequest request = requestBuilder.build();
        return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    private HttpRequest.Builder buildBaseRequest(String path) {
        var requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path));

        String jwtToken = AppState.getJwtToken();
        if (jwtToken != null) {
            requestBuilder.header("Authentication", jwtToken);
        }

        return requestBuilder;
    }
}