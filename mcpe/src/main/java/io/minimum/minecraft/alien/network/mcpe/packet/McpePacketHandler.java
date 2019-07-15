package io.minimum.minecraft.alien.network.mcpe.packet;

public interface McpePacketHandler {
    default void activated() {

    }

    default void deactivated() {

    }

    default void connected() {

    }

    default void disconnected() {

    }

    void exception(Throwable throwable);

    default void handleGeneric(Object message) {

    }

    default boolean handle(McpeLogin packet) {
        return false;
    }

    default boolean handle(McpeDisconnect packet) {
        return false;
    }

    default boolean handle(McpeServerToClientEncryptionHandshake packet) {
        return false;
    }

    default boolean handle(McpeClientToServerEncryptionHandshake packet) {
        return false;
    }

    default boolean handle(McpePlayStatus status) {
        return false;
    }

    default boolean handle(McpeResourcePacks packet) {
        return false;
    }

    default boolean handle(McpeResourcePackResponse packet) {
        return false;
    }

    default boolean handle(McpeResourcePackStack packStack) {
        return false;
    }
}
