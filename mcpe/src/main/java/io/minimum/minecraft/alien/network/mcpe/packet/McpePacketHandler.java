package io.minimum.minecraft.alien.network.mcpe.packet;

public interface McpePacketHandler {
    default void connected() {

    }

    default void disconnected() {

    }

    void exception(Throwable throwable);

    default void handleGeneric(Object message) {

    }

    default boolean handle(McpeLogin packet) {
        return true;
    }

    default boolean handle(McpeDisconnect packet) {
        return true;
    }

    default boolean handle(McpeServerToClientEncryptionHandshake packet) {
        return true;
    }

    default boolean handle(McpeClientToServerEncryptionHandshake packet) {
        return true;
    }

    default boolean handle(McpePlayStatus status) {
        return true;
    }

    default boolean handle(McpeResourcePacks packet) {
        return true;
    }

    default boolean handle(McpeResourcePackResponse packet) {
        return true;
    }
}
