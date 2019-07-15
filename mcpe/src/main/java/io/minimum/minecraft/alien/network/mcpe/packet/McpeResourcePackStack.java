package io.minimum.minecraft.alien.network.mcpe.packet;

import io.minimum.minecraft.alien.network.mcpe.util.Varints;
import io.netty.buffer.ByteBuf;

public class McpeResourcePackStack implements McpePacket {
    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(buf.readableBytes());
    }

    @Override
    public void encode(ByteBuf buf) {
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
