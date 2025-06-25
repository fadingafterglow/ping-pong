package ua.edu.ukma.cs.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameResultListResponse {
    private long total;
    private List<GameResultResponse> items;
}
