package ua.edu.ukma.cs.tcp.packets;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ua.edu.ukma.cs.enums.PacketType;

@Getter
@Setter
@Builder
@ToString
public class PacketOut {

    private PacketType type;
    private byte[] data;
}
