package ua.edu.ukma.cs.tcp.decoders;

import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.tcp.packets.PacketType;
import ua.edu.ukma.cs.tcp.packets.PacketIn;
import ua.edu.ukma.cs.utils.Crc16Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@RequiredArgsConstructor
public class PacketDecoder implements IDecoder<PacketIn> {

    @Override
    public PacketIn decode(ByteBuffer buffer) {
        int offset = buffer.position();
        buffer.mark();
        buffer.order(ByteOrder.BIG_ENDIAN);
        if (buffer.remaining() < 6) {
            buffer.reset();
            return null;
        }
        byte magicByte = buffer.get();
        PacketType type = PacketType.valueOf(buffer.get());
        short dataLength = buffer.getShort();
        short crc16Header = buffer.getShort();
        if (buffer.remaining() < dataLength + 2) {
            buffer.reset();
            return null;
        }
        byte[] data = new byte[dataLength];
        buffer.get(data);
        short crc16Data = buffer.getShort();

        Crc16Utils.validateCrc(crc16Header, buffer.array(), offset, 4);
        Crc16Utils.validateCrc(crc16Data, buffer.array(), offset + 6, dataLength);

        return PacketIn.builder()
                .magicByte(magicByte)
                .type(type)
                .dataLength(dataLength)
                .crc16Header(crc16Header)
                .data(data)
                .crc16Data(crc16Data)
                .build();
    }
}
