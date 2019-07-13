package io.minimum.minecraft.alien.network.mcpe.proxy.client.handler;

import io.minimum.minecraft.alien.network.mcpe.packet.McpePacketHandler;
import io.minimum.minecraft.alien.network.mcpe.proxy.player.McpePlayer;
import io.netty.util.ReferenceCountUtil;

public class MitmSessionHandler implements McpePacketHandler {
    private final McpePlayer player;

    public MitmSessionHandler(McpePlayer player) {
        this.player = player;
    }

    @Override
    public void exception(Throwable throwable) {

    }

    @Override
    public void handleGeneric(Object message) {
        player.getConnection().write(ReferenceCountUtil.retain(message));
    }
}
