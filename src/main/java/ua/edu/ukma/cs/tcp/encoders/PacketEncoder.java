package ua.edu.ukma.cs.tcp.encoders;

import lombok.RequiredArgsConstructor;
import ua.edu.ukma.cs.tcp.packets.PacketOut;
import ua.edu.ukma.cs.utils.Crc16Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@RequiredArgsConstructor
public class PacketEncoder implements IEncoder<PacketOut> {

    private static final byte MAGIC_BYTE = 0x42;

    @Override
    public ByteBuffer encode(PacketOut packet) {
        int size = 1 + 1 + 2 + 2 + packet.getData().length + 2;
        ByteBuffer buffer = ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN);
        return buffer.put(MAGIC_BYTE)
                .put(packet.getType().getCode())
                .putShort((short) packet.getData().length)
                .putShort(Crc16Utils.calculateCrc(buffer.array(), 0, 4))
                .put(packet.getData())
                .putShort(Crc16Utils.calculateCrc(packet.getData()));
    }
}
