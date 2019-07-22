package io.minimum.minecraft.alien.minecraft.bedrock.proxy.player.handler;

import io.minimum.minecraft.alien.minecraft.bedrock.packet.McpePacketHandler;
import io.minimum.minecraft.alien.minecraft.bedrock.pipeline.BedrockConnection;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.McpeServerConnection;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.player.McpePlayer;
import io.netty.util.ReferenceCountUtil;

public class PlaySessionHandler implements McpePacketHandler {
    private final McpePlayer player;

    public PlaySessionHandler(McpePlayer player) {
        this.player = player;
    }

    @Override
    public void disconnected() {
        McpeServerConnection connection = player.getCurrentServer();
        if (connection != null) {
            connection.disconnect();
        }
    }

    @Override
    public void exception(Throwable throwable) {
        player.getConnection().closeWith("Internal proxy error");
    }

    @Override
    public void handleGeneric(Object message) {
        McpeServerConnection connection = player.getCurrentServer();
        if (connection != null) {
            BedrockConnection remoteConn = connection.getConnection();
            if (remoteConn != null) {
                remoteConn.write(ReferenceCountUtil.retain(message));
            }
        }
    }
}
