package ua.edu.ukma.cs.pages;

import ua.edu.ukma.cs.api.request.GameResultFilterDto;
import ua.edu.ukma.cs.app.PingPongClient;
import ua.edu.ukma.cs.services.GamesResultsService;

public class GamesResultsPage extends BasePage {

    private final GamesResultsService gamesResultsService;

    public GamesResultsPage(PingPongClient app, GamesResultsService gamesResultsService) {
        super(app);
        this.gamesResultsService = gamesResultsService;
    }

    @Override
    public void init() {
        System.out.println(gamesResultsService.getGameResultsByFilter(GameResultFilterDto.builder().minThisUserScore(5).build()));
    }
}
