package ua.edu.ukma.cs.tcp.decoders;

import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.enums.PacketType;
import ua.edu.ukma.cs.tcp.packets.PacketIn;
import ua.edu.ukma.cs.utils.Crc16Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

@RequiredArgsConstructor
public class PacketDecoder implements IDecoder<PacketIn> {

    @Override
    public PacketIn decode(ByteBuffer buffer) {
        int offset = buffer.position();
        buffer.mark();
        buffer.order(ByteOrder.BIG_ENDIAN);
        if (buffer.remaining() < 26) {
            buffer.reset();
            return null;
        }
        byte magicByte = buffer.get();
        PacketType type = PacketType.valueOf(buffer.get());
        UUID gameLobbyId = new UUID(buffer.getLong(), buffer.getLong());
        int userId = buffer.getInt();
        short dataLength = buffer.getShort();
        short crc16Header = buffer.getShort();
        if (buffer.remaining() < dataLength + 2) {
            buffer.reset();
            return null;
        }
        byte[] data = new byte[dataLength];
        buffer.get(data);
        short crc16Data = buffer.getShort();

        Crc16Utils.validateCrc(crc16Header, buffer.array(), offset, 24);
        Crc16Utils.validateCrc(crc16Data, buffer.array(), offset + 26, dataLength);

        return PacketIn.builder()
                .magicByte(magicByte)
                .type(type)
                .gameLobbyId(gameLobbyId)
                .userId(userId)
                .dataLength(dataLength)
                .crc16Header(crc16Header)
                .data(data)
                .crc16Data(crc16Data)
                .build();
    }
}
