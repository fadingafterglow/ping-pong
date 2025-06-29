package ua.edu.ukma.cs.tcp.packets;

import lombok.Getter;

@Getter
public enum PacketType {
    JOIN_LOBBY_REQUEST((byte) -1),
    START_GAME_REQUEST((byte) -2),
    MOVE_RACKET_REQUEST((byte) -3),

    BAD_PAYLOAD_RESPONSE((byte) 1),
    JOIN_LOBBY_RESPONSE((byte) 2),

    GAME_LOBBY_STATE_UPDATE((byte) 51),
    GAME_STATE_UPDATE((byte) 52);

    private final byte code;

    PacketType(byte code) {
        this.code = code;
    }

    public static PacketType valueOf(byte code) {
        for (PacketType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }
}
