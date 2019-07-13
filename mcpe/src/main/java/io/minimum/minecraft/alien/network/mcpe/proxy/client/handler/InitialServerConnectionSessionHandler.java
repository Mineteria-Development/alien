package io.minimum.minecraft.alien.network.mcpe.proxy.client.handler;

import io.minimum.minecraft.alien.network.mcpe.listener.McpeConnection;
import io.minimum.minecraft.alien.network.mcpe.packet.McpePacketHandler;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeResourcePackResponse;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeResourcePacks;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeServerToClientEncryptionHandshake;
import io.minimum.minecraft.alien.network.mcpe.proxy.player.McpePlayer;

public class InitialServerConnectionSessionHandler implements McpePacketHandler {
    private final McpeConnection remoteServer;
    private final McpePlayer player;

    public InitialServerConnectionSessionHandler(McpeConnection remoteServer, McpePlayer player) {
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
        remoteServer.write(new McpeResourcePackResponse(McpeResourcePackResponse.ACCEPTED));
        remoteServer.write(new McpeResourcePackResponse(McpeResourcePackResponse.HAVE_PACKS));
        remoteServer.write(new McpeResourcePackResponse(McpeResourcePackResponse.COMPLETED));

        return true;
    }
}
