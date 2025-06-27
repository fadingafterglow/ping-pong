package ua.edu.ukma.cs.services;

import lombok.RequiredArgsConstructor;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateLobbyService {
    private final HttpService httpService;

    public UUID createLobby() {
        HttpResponse<byte[]> createLobbyResponse = httpService.post("/game");
        return UUID.fromString(new String(createLobbyResponse.body(), StandardCharsets.UTF_8));
    }
}