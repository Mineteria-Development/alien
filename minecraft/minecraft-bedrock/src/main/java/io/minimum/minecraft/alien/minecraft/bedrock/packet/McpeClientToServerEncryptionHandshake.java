package io.minimum.minecraft.alien.minecraft.bedrock.packet;

import io.netty.buffer.ByteBuf;

public class McpeClientToServerEncryptionHandshake implements McpePacket {
    @Override
    public void decode(ByteBuf buf, int protocolVersion) {

    }

    @Override
    public void encode(ByteBuf buf, int protocolVersion) {

    }

    @Override
    public boolean handle(McpePacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public String toString() {
        return "McpeClientToServerEncryptionHandshake";
    }
}
