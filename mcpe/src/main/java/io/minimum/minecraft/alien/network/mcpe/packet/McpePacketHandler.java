package io.minimum.minecraft.alien.network.mcpe.packet;

public interface McpePacketHandler {
    void connected();

    void disconnected();

    void handle(McpeLogin packet);

    void handle(McpeDisconnect mcpeDisconnect);
}
