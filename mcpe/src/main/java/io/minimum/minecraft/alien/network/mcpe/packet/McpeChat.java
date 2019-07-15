package io.minimum.minecraft.alien.network.mcpe.packet;

import io.minimum.minecraft.alien.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;

public class McpeChat implements McpePacket {
    public static final int TYPE_RAW = 0;
    public static final int TYPE_CHAT = 1;
    public static final int TYPE_TRANSLATION = 2;
    public static final int TYPE_POPUP = 3;
    public static final int TYPE_JUKEBOX_POPUP = 4;
    public static final int TYPE_TIP = 5;
    public static final int TYPE_SYSTEM = 6;
    public static final int TYPE_WHISPER = 7;
    public static final int TYPE_ANNOUNCEMENT = 8;
    public static final int TYPE_JSON = 9;

    private int type;
    private String message;

    @Override
    public void decode(ByteBuf buf) {
        // We should really not decode chat packets
        throw new UnsupportedOperationException();
    }

    @Override
    public void encode(ByteBuf buf) {
        // TODO: Fuller chat support?
        buf.writeByte(type);
        buf.writeBoolean(false);
        McpeUtil.writeVarintLengthString(buf, message);
        McpeUtil.writeVarintLengthString(buf, "");
        McpeUtil.writeVarintLengthString(buf, "");
    }

    @Override
    public boolean handle(McpePacketHandler handler) {
        return handler.handle(this);
    }

    public McpeChat(int type, String message) {
        this.type = type;
        this.message = message;
    }

    @Override
    public String toString() {
        return "McpeChat{" +
                "type=" + type +
                ", message='" + message + '\'' +
                '}';
    }
}
