package io.minimum.minecraft.alien.network.mcpe.packet;

import io.minimum.minecraft.alien.network.mcpe.util.Varints;
import io.netty.buffer.ByteBuf;

public class McpePlayStatus implements McpePacket {
    public static final int SUCCESS = 0;
    public static final int PLAYER_SPAWN = 3;

    private int status;

    public McpePlayStatus() {
    }

    public McpePlayStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void decode(ByteBuf buf) {
        this.status = (int) Varints.decodeUnsigned(buf);
    }

    @Override
    public void encode(ByteBuf buf) {
        Varints.encodeSigned(buf, this.status);
    }

    @Override
    public boolean handle(McpePacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public String toString() {
        return "McpePlayStatus{" +
                "status=" + status +
                '}';
    }
}
