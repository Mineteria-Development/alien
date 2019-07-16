package io.minimum.minecraft.alien.network.mcpe.proxy.client.handler;

import io.minimum.minecraft.alien.network.mcpe.packet.*;
import io.minimum.minecraft.alien.network.mcpe.proxy.client.McpeServerConnection;
import io.minimum.minecraft.alien.network.mcpe.proxy.player.McpePlayer;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerPlaySessionHandler implements McpePacketHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServerPlaySessionHandler.class);

    private final McpePlayer player;
    private final McpeServerConnection connection;

    public ServerPlaySessionHandler(McpePlayer player, McpeServerConnection connection) {
        this.player = player;
        this.connection = connection;
    }

    @Override
    public void activated() {
    }

    @Override
    public void exception(Throwable throwable) {
        handleDisconnect();
    }

    @Override
    public void disconnected() {
        handleDisconnect();
    }

    @Override
    public boolean handle(McpeDisconnect packet) {
        handleDisconnect();
        return true;
    }

    private void handleDisconnect() {
        // We were disconnected from the server. For now, kick the player.
        player.getConnection().closeWith("An error occurred in your connection, check your console logs.");
    }

    @Override
    public boolean handle(McpeResourcePacks packet) {
        connection.getConnection().write(new McpeResourcePackResponse(McpeResourcePackResponse.HAVE_PACKS));
        return true;
    }

    @Override
    public boolean handle(McpeResourcePackStack packStack) {
        connection.getConnection().write(new McpeResourcePackResponse(McpeResourcePackResponse.COMPLETED));
        return true;
    }

    @Override
    public void handleGeneric(Object message) {
        player.getConnection().write(ReferenceCountUtil.retain(message));
    }
}
