package io.minimum.minecraft.alien.network.mcpe.packet;

public interface McpePacketHandler {
    default void connected() {

    }

    default void disconnected() {

    }

    void exception(Throwable throwable);

    default void handle(McpeLogin packet) {

    }

    default void handle(McpeDisconnect packet) {

    }

    default void handle(McpeServerToClientEncryptionHandshake packet) {

    }

    default void handle(McpeClientToServerEncryptionHandshake packet) {

    }

    default void handle(McpePlayStatus status) {

    }

    default void handle(McpeResourcePacks packet) {

    }

    default void handle(McpeResourcePackResponse packet) {

    }
}
