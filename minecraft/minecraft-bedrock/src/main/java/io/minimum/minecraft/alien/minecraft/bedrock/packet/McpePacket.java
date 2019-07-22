package io.minimum.minecraft.alien.minecraft.bedrock.packet;

import io.netty.buffer.ByteBuf;

public interface McpePacket {
    void decode(ByteBuf buf, int protocolVersion);

    void encode(ByteBuf buf, int protocolVersion);

    boolean handle(McpePacketHandler handler);
}
