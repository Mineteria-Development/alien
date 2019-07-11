package io.minimum.minecraft.alien.network.mcpe.packet;

public interface McpePacketHandler {
    void connected();

    void disconnected();

    default void handle(McpeLogin packet) {

    }

    default void handle(McpeDisconnect packet) {

    }

    default void handle(McpeServerToClientEncryptionHandshake packet) {

    }

    default void handle(McpeClientToServerEncryptionHandshake packet) {

    }
}
