package ua.edu.ukma.cs.tcp.packets;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class PacketOut {

    private PacketType type;
    private byte[] data;
}
