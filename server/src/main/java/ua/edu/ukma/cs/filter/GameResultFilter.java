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
    private String username;
    private Integer minThisUserScore;
    private Integer maxThisUserScore;
    private Integer minOtherUserScore;
    private Integer maxOtherUserScore;
    private LocalDateTime minTimeFinished;
    private LocalDateTime maxTimeFinished;

    public static GameResultFilter fromDto(GameResultFilterDto dto, int userId) {
        return GameResultFilter.builder()
                .userId(userId)
                .username(dto.getUsername())
                .minThisUserScore(dto.getMinThisUserScore())
                .maxThisUserScore(dto.getMaxThisUserScore())
                .minOtherUserScore(dto.getMinOtherUserScore())
                .maxOtherUserScore(dto.getMaxOtherUserScore())
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
                .expression(username, fieldExpressionMap.get("username"))
                .min(minThisUserScore, fieldExpressionMap.get("thisUserScore"))
                .max(maxThisUserScore, fieldExpressionMap.get("thisUserScore"))
                .min(minOtherUserScore, fieldExpressionMap.get("otherUserScore"))
                .max(maxOtherUserScore, fieldExpressionMap.get("otherUserScore"))
                .min(minTimeFinished, fieldExpressionMap.get("timeFinished"))
                .max(maxTimeFinished, fieldExpressionMap.get("timeFinished"))
                .getConditions();
    }

    @Override
    public void setParameters(PreparedStatement st, int parametersIndexOffset) {
        ParametersSetter setter = new ParametersSetter(st, parametersIndexOffset)
                .setInt(userId)
                .setInt(userId)
                .setLikeString(username)
                .setLikeString(username);
        if (minThisUserScore != null)
            setter.setInt(userId).setInt(minThisUserScore);
        if (maxThisUserScore != null)
            setter.setInt(userId).setInt(maxThisUserScore);
        if (minOtherUserScore != null)
            setter.setInt(userId).setInt(minOtherUserScore);
        if (maxOtherUserScore != null)
            setter.setInt(userId).setInt(maxOtherUserScore);
        setter.setTimestamp(minTimeFinished)
                .setTimestamp(maxTimeFinished);
    }
}
