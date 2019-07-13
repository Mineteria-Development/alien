package io.minimum.minecraft.alien.network.mcpe.packet;

import io.netty.buffer.ByteBuf;

public class McpeResourcePacks implements McpePacket {
    @Override
    public void decode(ByteBuf buf) {
        // We can't do anything useful with this packet
        buf.skipBytes(buf.readableBytes());
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeBoolean(false); // require the client to accept the resource pack
        buf.writeBoolean(false); // scripting support, we probably don't need this
        buf.writeShortLE(0); // behavior packs
        buf.writeShortLE(0); // resource packs
    }

    @Override
    public boolean handle(McpePacketHandler handler) {
        return handler.handle(this);
    }
}
