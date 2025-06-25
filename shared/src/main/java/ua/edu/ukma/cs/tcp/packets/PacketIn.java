package ua.edu.ukma.cs.tcp.packets;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PacketIn {

    private final byte magicByte;
    private final PacketType type;
    private final short dataLength;
    private final short crc16Header;
    private final byte[] data;
    private final short crc16Data;
}
