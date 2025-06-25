package ua.edu.ukma.cs.mapping;

import ua.edu.ukma.cs.api.response.GameResultResponse;
import ua.edu.ukma.cs.entity.GameResultEntity;

public class GameResultMapper {
    public GameResultResponse toResponse(GameResultEntity entity) {
        return GameResultResponse.builder()
                .id(entity.getId())
                .creatorScore(entity.getCreatorScore())
                .otherScore(entity.getOtherScore())
                .timeFinished(entity.getTimeFinished())
                .creatorId(entity.getCreatorId())
                .otherUserId(entity.getOtherUserId())
                .build();
    }
}
