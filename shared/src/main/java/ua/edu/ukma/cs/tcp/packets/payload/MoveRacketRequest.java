package ua.edu.ukma.cs.tcp.packets.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveRacketRequest {
    private boolean up;
}
