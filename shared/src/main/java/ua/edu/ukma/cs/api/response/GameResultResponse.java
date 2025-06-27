package ua.edu.ukma.cs.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameResultResponse {
    private int id;
    private int creatorScore;
    private int otherScore;
    private LocalDateTime timeFinished;
    private int creatorId;
    private String creatorUsername;
    private int otherUserId;
    private String otherUsername;
}
