package io.minimum.minecraft.alien.network.mcpe.packet;

public interface McpePacketHandler {
    void handle(McpeLogin packet);

    void connected();

    void disconnected();
}
