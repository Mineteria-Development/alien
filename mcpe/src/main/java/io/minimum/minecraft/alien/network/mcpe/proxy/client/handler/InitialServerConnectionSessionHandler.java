package io.minimum.minecraft.alien.network.mcpe.proxy.client.handler;

import io.minimum.minecraft.alien.network.mcpe.pipeline.McpeConnection;
import io.minimum.minecraft.alien.network.mcpe.packet.*;
import io.minimum.minecraft.alien.network.mcpe.proxy.client.McpeServerConnection;
import io.minimum.minecraft.alien.network.mcpe.proxy.player.handler.MitmWritingSessionHandler;
import io.minimum.minecraft.alien.network.mcpe.proxy.player.McpePlayer;

public class InitialServerConnectionSessionHandler implements McpePacketHandler {
    private final McpeServerConnection remoteServer;
    private final McpePlayer player;

    public InitialServerConnectionSessionHandler(McpeServerConnection remoteServer, McpePlayer player) {
        this.remoteServer = remoteServer;
        this.player = player;
    }

    @Override
    public void exception(Throwable throwable) {
        // TODO: Do something here
    }

    @Override
    public boolean handle(McpeServerToClientEncryptionHandshake packet) {
        throw new IllegalStateException("Proxy does not support encryption yet!");
    }

    @Override
    public boolean handle(McpeResourcePacks packet) {
        // pretend we have the resource pack
        remoteServer.getConnection().write(new McpeResourcePackResponse(McpeResourcePackResponse.ACCEPTED));
        remoteServer.getConnection().write(new McpeResourcePackResponse(McpeResourcePackResponse.HAVE_PACKS));
        remoteServer.getConnection().write(new McpeResourcePackResponse(McpeResourcePackResponse.COMPLETED));

        return true;
    }

    @Override
    public boolean handle(McpePlayStatus status) {
        if (status.getStatus() == McpePlayStatus.PLAYER_SPAWN) {
            // Forward this packet to the player...
            player.getConnection().write(status);

            // And switch to the MITM state
            remoteServer.getConnection().setSessionHandler(new MitmSessionHandler(player));
            player.getConnection().setSessionHandler(new MitmWritingSessionHandler(remoteServer));
        }
        return true;
    }
}
