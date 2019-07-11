package io.minimum.minecraft.alien.network.mcpe.packet;

import io.netty.buffer.ByteBuf;

public class McpeClientToServerEncryptionHandshake implements McpePacket {
    @Override
    public void decode(ByteBuf buf) {

    }

    @Override
    public void encode(ByteBuf buf) {

    }

    @Override
    public void handle(McpePacketHandler handler) {
        handler.handle(this);
    }

    @Override
    public String toString() {
        return "McpeClientToServerEncryptionHandshake";
    }
}
