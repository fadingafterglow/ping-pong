package ua.edu.ukma.cs.tcp.packets.payload;

import lombok.Data;

@Data
public class MoveRacketRequest {
    private final boolean up;
}
