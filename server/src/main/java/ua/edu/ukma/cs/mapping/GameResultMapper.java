package ua.edu.ukma.cs.mapping;

import ua.edu.ukma.cs.api.response.GameResultListResponse;
import ua.edu.ukma.cs.api.response.GameResultResponse;
import ua.edu.ukma.cs.entity.GameResultEntity;

import java.util.List;

public class GameResultMapper {
    public GameResultResponse toResponse(GameResultEntity entity) {
        return GameResultResponse.builder()
                .id(entity.getId())
                .creatorScore(entity.getCreatorScore())
                .otherScore(entity.getOtherScore())
                .timeFinished(entity.getTimeFinished())
                .creatorId(entity.getCreatorId())
                .creatorUsername(entity.getCreatorUsername())
                .otherUserId(entity.getOtherUserId())
                .otherUsername(entity.getOtherUsername())
                .build();
    }

    public GameResultListResponse toListResponse(long total, List<GameResultEntity> items) {
        return GameResultListResponse.builder()
                .total(total)
                .items(items.stream().map(this::toResponse).toList())
                .build();
    }
}
