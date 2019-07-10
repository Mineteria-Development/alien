package io.minimum.minecraft.alien.network.mcpe.packet;

import io.netty.buffer.ByteBuf;

public interface McpePacket {
    void decode(ByteBuf buf);

    void encode(ByteBuf buf);

    void handle(McpePacketHandler handler);
}
