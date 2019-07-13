package io.minimum.minecraft.alien.network.mcpe.proxy.player.handler;

import io.minimum.minecraft.alien.network.mcpe.packet.McpePacketHandler;
import io.minimum.minecraft.alien.network.mcpe.pipeline.McpeConnection;
import io.minimum.minecraft.alien.network.mcpe.proxy.client.McpeServerConnection;
import io.netty.util.ReferenceCountUtil;

public class MitmWritingSessionHandler implements McpePacketHandler {
    private final McpeServerConnection connection;

    public MitmWritingSessionHandler(McpeServerConnection connection) {
        this.connection = connection;
    }

    @Override
    public void exception(Throwable throwable) {

    }

    @Override
    public void handleGeneric(Object message) {
        McpeConnection remote = connection.getConnection();
        if (remote != null) {
            remote.write(ReferenceCountUtil.retain(message));
        }
    }
}
