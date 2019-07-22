package io.minimum.minecraft.alien.minecraft.bedrock.packet;

import io.minimum.minecraft.alien.minecraft.shared.codec.Varints;
import io.netty.buffer.ByteBuf;

public class McpeResourcePackStack implements McpePacket {
    @Override
    public void decode(ByteBuf buf, int protocolVersion) {
        buf.skipBytes(buf.readableBytes());
    }

    @Override
    public void encode(ByteBuf buf, int protocolVersion) {
        buf.writeBoolean(false);
        Varints.encodeUnsigned(buf, 0);
        Varints.encodeUnsigned(buf, 0);
        buf.writeBoolean(false);
    }

    @Override
    public boolean handle(McpePacketHandler handler) {
        return handler.handle(this);
    }
}
