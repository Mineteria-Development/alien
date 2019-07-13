package io.minimum.minecraft.alien.network.mcpe.proxy.client.handler;

import io.minimum.minecraft.alien.network.mcpe.packet.McpePacketHandler;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeResourcePackResponse;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeResourcePacks;
import io.minimum.minecraft.alien.network.mcpe.pipeline.McpeConnection;
import io.minimum.minecraft.alien.network.mcpe.proxy.client.McpeServerConnection;
import io.minimum.minecraft.alien.network.mcpe.proxy.player.McpePlayer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.ReferenceCountUtil;

public class ServerPlaySessionHandler implements McpePacketHandler {
    private final McpePlayer player;
    private final McpeServerConnection connection;

    public ServerPlaySessionHandler(McpePlayer player, McpeServerConnection connection) {
        this.player = player;
        this.connection = connection;
    }

    @Override
    public void exception(Throwable throwable) {

    }

    @Override
    public boolean handle(McpeResourcePacks packet) {
        // pretend we have the resource pack
        connection.getConnection().write(new McpeResourcePackResponse(McpeResourcePackResponse.ACCEPTED));
        connection.getConnection().write(new McpeResourcePackResponse(McpeResourcePackResponse.HAVE_PACKS));
        connection.getConnection().write(new McpeResourcePackResponse(McpeResourcePackResponse.COMPLETED));

        return true;
    }

    @Override
    public void handleGeneric(Object message) {
        if (message instanceof ByteBuf) {
            System.out.println("[IN] " + ByteBufUtil.prettyHexDump((ByteBuf) message));
        }
        player.getConnection().write(ReferenceCountUtil.retain(message));
    }
}
