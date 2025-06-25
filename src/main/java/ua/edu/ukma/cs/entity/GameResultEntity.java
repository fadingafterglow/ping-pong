package ua.edu.ukma.cs.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameResultEntity {
    private Integer id;
    private int creatorScore;
    private int otherScore;
    private LocalDateTime timeFinished;
    private int creatorId;
    private int otherUserId;
}
