package io.minimum.minecraft.alien.minecraft.bedrock.proxy.player.handler;

import io.minimum.minecraft.alien.minecraft.bedrock.packet.*;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.McpeServerConnection;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.data.ServerInfo;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.player.McpePlayer;

import java.net.InetSocketAddress;

class ResourcePackPacketHandler implements McpePacketHandler {
    private final McpePlayer player;

    ResourcePackPacketHandler(McpePlayer player) {
        this.player = player;
    }

    @Override
    public void activated() {
        player.getConnection().write(new McpePlayStatus(McpePlayStatus.SUCCESS));
        player.getConnection().write(new McpeResourcePacks());
    }

    @Override
    public void exception(Throwable throwable) {

    }

    @Override
    public boolean handle(McpeResourcePackResponse packet) {
        if (packet.getStatus() == McpeResourcePackResponse.HAVE_PACKS) {
            player.getConnection().write(new McpeResourcePackStack());
        } else if (packet.getStatus() == McpeResourcePackResponse.COMPLETED || packet.getStatus() == McpeResourcePackResponse.REJECTED){
            doLogin();
        }
        return true;
    }

    private void doLogin() {
        player.getConnection().setSessionHandler(new NoopState());
        new McpeServerConnection(new ServerInfo("test", new InetSocketAddress("127.0.0.1", 25565)), player)
                .connect()
                .addListener(result -> {
                    if (!result.isSuccess()) {
                        player.getConnection().closeWith("Connection failed.");
                        result.cause().printStackTrace();
                    }
                });
    }
}
