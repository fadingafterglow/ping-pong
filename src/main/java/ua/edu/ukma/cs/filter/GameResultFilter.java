package ua.edu.ukma.cs.filter;

import lombok.experimental.SuperBuilder;
import ua.edu.ukma.cs.api.request.GameResultFilterDto;
import ua.edu.ukma.cs.filter.base.BaseFilter;
import ua.edu.ukma.cs.filter.base.ConditionsBuilder;
import ua.edu.ukma.cs.filter.base.ParametersSetter;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@SuperBuilder
public class GameResultFilter extends BaseFilter {
    private Integer userId;
    private Integer minScore;
    private Integer maxScore;
    private LocalDateTime minTimeFinished;
    private LocalDateTime maxTimeFinished;

    public static GameResultFilter fromDto(GameResultFilterDto dto, int userId) {
        return GameResultFilter.builder()
                .userId(userId)
                .minScore(dto.getMinScore())
                .maxScore(dto.getMaxScore())
                .minTimeFinished(dto.getMinTimeFinished())
                .maxTimeFinished(dto.getMaxTimeFinished())
                .page(dto.getPage())
                .size(dto.getSize())
                .sortBy(dto.getSortBy())
                .descendingOrder(dto.isDescendingOrder())
                .build();
    }

    @Override
    protected List<String> formWhereConditions(Map<String, String> fieldExpressionMap) {
        return new ConditionsBuilder()
                .expression(userId, fieldExpressionMap.get("userId"))
                .min(minScore, fieldExpressionMap.get("score"))
                .max(maxScore, fieldExpressionMap.get("score"))
                .min(minTimeFinished, fieldExpressionMap.get("timeFinished"))
                .max(maxTimeFinished, fieldExpressionMap.get("timeFinished"))
                .getConditions();
    }

    @Override
    public void setParameters(PreparedStatement st, int parametersIndexOffset) {
        new ParametersSetter(st, parametersIndexOffset)
                .setInt(userId)
                .setInt(userId)
                .setInt(minScore)
                .setInt(maxScore)
                .setTimestamp(minTimeFinished)
                .setTimestamp(maxTimeFinished);
    }
}
