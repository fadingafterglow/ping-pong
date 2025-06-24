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
    private Integer minScore;
    private Integer maxScore;
    private LocalDateTime minTimeFinished;
    private LocalDateTime maxTimeFinished;
    private int page;
    private int size;
    private String sortBy;
    private boolean descendingOrder;
}
