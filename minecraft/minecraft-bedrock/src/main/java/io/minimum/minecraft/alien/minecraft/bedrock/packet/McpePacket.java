package io.minimum.minecraft.alien.minecraft.bedrock.packet;

import io.netty.buffer.ByteBuf;

public interface McpePacket {
    void decode(ByteBuf buf);

    void encode(ByteBuf buf);

    boolean handle(McpePacketHandler handler);
}
