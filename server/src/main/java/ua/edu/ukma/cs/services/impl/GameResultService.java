package ua.edu.ukma.cs.services.impl;

import ua.edu.ukma.cs.api.request.GameResultFilterDto;
import ua.edu.ukma.cs.api.response.GameResultListResponse;
import ua.edu.ukma.cs.api.response.GameResultResponse;
import ua.edu.ukma.cs.api.response.GameResultStatsResponse;
import ua.edu.ukma.cs.database.transaction.TransactionDelegate;
import ua.edu.ukma.cs.entity.GameResultEntity;
import ua.edu.ukma.cs.entity.GameResultStats;
import ua.edu.ukma.cs.exception.ForbiddenException;
import ua.edu.ukma.cs.exception.NotFoundException;
import ua.edu.ukma.cs.filter.GameResultFilter;
import ua.edu.ukma.cs.game.lobby.GameLobbySnapshot;
import ua.edu.ukma.cs.game.state.GameStateSnapshot;
import ua.edu.ukma.cs.mapping.GameResultMapper;
import ua.edu.ukma.cs.repository.GameResultRepository;
import ua.edu.ukma.cs.repository.GameResultStatsRepository;
import ua.edu.ukma.cs.security.SecurityContext;
import ua.edu.ukma.cs.services.IGameResultService;
import ua.edu.ukma.cs.utils.TimeUtils;

import java.util.List;

public class GameResultService implements IGameResultService {

    private final GameResultRepository repository;
    private final GameResultStatsRepository statsRepository;
    private final TransactionDelegate transactionDelegate;
    private final TransactionDelegate readOnlyTransactionDelegate;
    private final GameResultMapper mapper;

    public GameResultService(GameResultRepository repository, GameResultStatsRepository statsRepository) {
        this.repository = repository;
        this.statsRepository = statsRepository;
        this.transactionDelegate = new TransactionDelegate();
        this.readOnlyTransactionDelegate = new TransactionDelegate(true);
        this.mapper = new GameResultMapper();
    }

    @Override
    public void saveGameResult(GameLobbySnapshot gameLobby, GameStateSnapshot gameState) {
        transactionDelegate.runInTransaction(() -> {
            GameResultEntity entity = GameResultEntity.builder()
                    .creatorScore(gameState.player1Score())
                    .otherScore(gameState.player2Score())
                    .timeFinished(TimeUtils.getCurrentDateTimeUTC())
                    .creatorId(gameLobby.creatorId())
                    .otherUserId(gameLobby.otherPlayerId())
                    .build();
            repository.create(entity);
        });
    }

    @Override
    public GameResultResponse getById(int id, SecurityContext securityContext) {
        return readOnlyTransactionDelegate.runInTransaction(() -> {
            GameResultEntity entity = repository.getById(id).orElseThrow(NotFoundException::new);
            int currentUserId = securityContext.getUserId();
            if (entity.getCreatorId() != currentUserId && entity.getOtherUserId() != currentUserId) {
                throw new ForbiddenException();
            }
            return mapper.toResponse(entity);
        });
    }

    @Override
    public GameResultListResponse getCurrentUserGameResults(SecurityContext securityContext, GameResultFilterDto filterDto) {
        return readOnlyTransactionDelegate.runInTransaction(() -> {
            int currentUserId = securityContext.getUserId();
            GameResultFilter filter = GameResultFilter.fromDto(filterDto, currentUserId);
            List<GameResultEntity> results = repository.getAllByFilter(filter);
            long total = repository.countByFilter(filter);
            return mapper.toListResponse(total, results);
        });
    }

    @Override
    public GameResultStatsResponse getCurrentUserGameResultStats(SecurityContext securityContext) {
        return readOnlyTransactionDelegate.runInTransaction(() -> {
            int currentUserID = securityContext.getUserId();
            GameResultStats stats = statsRepository.getStatsForUser(currentUserID)
                    .orElse(new GameResultStats(0, 0, 0));
            return GameResultStatsResponse.builder()
                    .totalGames(stats.getTotalGames())
                    .wins(stats.getWins())
                    .averageScore(stats.getAverageScore())
                    .build();
        });
    }
}
