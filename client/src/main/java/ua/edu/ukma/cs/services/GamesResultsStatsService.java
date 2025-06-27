package ua.edu.ukma.cs.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ua.edu.ukma.cs.api.response.GameResultStatsResponse;
import ua.edu.ukma.cs.utils.ObjectMapperHolder;

import java.net.http.HttpResponse;

@RequiredArgsConstructor
public class GamesResultsStatsService {

    private final HttpService httpService;

    @SneakyThrows
    public GameResultStatsResponse getGameResultsStats() {
        HttpResponse<byte[]> response = httpService.get("/game-result/stats");
        return ObjectMapperHolder.get().readValue(response.body(), GameResultStatsResponse.class);
    }
}