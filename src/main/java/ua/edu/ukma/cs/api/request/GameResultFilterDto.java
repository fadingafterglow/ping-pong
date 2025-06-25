package ua.edu.ukma.cs.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameResultFilterDto {
    private Integer minThisUserScore;
    private Integer maxThisUserScore;
    private Integer minOtherUserScore;
    private Integer maxOtherUserScore;
    private LocalDateTime minTimeFinished;
    private LocalDateTime maxTimeFinished;
    private int page;
    private int size;
    private String sortBy;
    private boolean descendingOrder;
}
