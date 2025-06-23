package ua.edu.ukma.cs.tcp.packets;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import ua.edu.ukma.cs.enums.PacketType;

import java.util.UUID;

@Getter
@Builder
@ToString
public class PacketIn {

    private final byte magicByte;
    private final PacketType type;
    private final UUID gameLobbyId;
    private final int userId;
    private final short dataLength;
    private final short crc16Header;
    private final byte[] data;
    private final short crc16Data;
}
