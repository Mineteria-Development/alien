package io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.handler;

import io.minimum.minecraft.alien.minecraft.bedrock.packet.McpeDisconnect;
import io.minimum.minecraft.alien.minecraft.bedrock.packet.McpePacketHandler;
import io.minimum.minecraft.alien.minecraft.bedrock.packet.McpePlayStatus;
import io.minimum.minecraft.alien.minecraft.bedrock.packet.McpeServerToClientEncryptionHandshake;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.McpeServerConnection;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.player.handler.PlaySessionHandler;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.player.McpePlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InitialServerConnectionSessionHandler implements McpePacketHandler {
    private static final Logger LOGGER = LogManager.getLogger(InitialServerConnectionSessionHandler.class);

    private final McpeServerConnection remoteServer;
    private final McpePlayer player;

    public InitialServerConnectionSessionHandler(McpeServerConnection remoteServer, McpePlayer player) {
        this.remoteServer = remoteServer;
        this.player = player;
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
        player.getConnection().closeWith("Unable to connect to the server. Try again later.");
        remoteServer.disconnect();
    }

    @Override
    public boolean handle(McpeServerToClientEncryptionHandshake packet) {
        throw new IllegalStateException("Proxy does not support encryption yet!");
    }

    @Override
    public boolean handle(McpePlayStatus status) {
        if (status.getStatus() == McpePlayStatus.SUCCESS) {
            // Switch to the MITM state
            remoteServer.getConnection().setSessionHandler(new ServerPlaySessionHandler(player, remoteServer));

            player.setCurrentServer(remoteServer);

            player.getConnection().setSessionHandler(new PlaySessionHandler(player));
        }
        return true;
    }
}
