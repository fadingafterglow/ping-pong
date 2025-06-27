package ua.edu.ukma.cs.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ua.edu.ukma.cs.api.request.GameResultFilterDto;
import ua.edu.ukma.cs.api.response.GameResultListResponse;
import ua.edu.ukma.cs.utils.ObjectMapperHolder;

import java.net.http.HttpResponse;

@RequiredArgsConstructor
public class GamesResultsService {

    private final HttpService httpService;

    @SneakyThrows
    public GameResultListResponse getGameResultsByFilter(GameResultFilterDto filter) {
        HttpResponse<byte[]> httpResponse = httpService.get("/game-result", filter);
        return ObjectMapperHolder.get().readValue(httpResponse.body(), GameResultListResponse.class);
    }
}