package io.minimum.minecraft.alien.minecraft.bedrock.packet;

import io.netty.buffer.ByteBuf;

public class McpeClientToServerEncryptionHandshake implements McpePacket {
    @Override
    public void decode(ByteBuf buf) {

    }

    @Override
    public void encode(ByteBuf buf) {

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
