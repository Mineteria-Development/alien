package io.minimum.minecraft.alien.network.mcpe.packet;

import io.netty.buffer.ByteBuf;

public class McpeResourcePackResponse implements McpePacket {
    public static final int REJECTED = 0;
    public static final int ACCEPTED = 1;
    public static final int HAVE_PACKS = 2;
    public static final int COMPLETED = 3;

    private int status;

    public McpeResourcePackResponse() {
    }

    public McpeResourcePackResponse(int status) {
        this.status = status;
    }

    @Override
    public void decode(ByteBuf buf) {
        this.status = buf.readUnsignedByte();
        buf.skipBytes(buf.readableBytes());
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeByte(status);
        buf.writeShortLE(0);
    }

    public int getStatus() {
        return status;
    }

    @Override
    public boolean handle(McpePacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public String toString() {
        return "McpeResourcePackResponse{" +
                "status=" + status +
                '}';
    }
}
