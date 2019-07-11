package io.minimum.minecraft.alien.network.mcpe.proxy.handler;

import io.minimum.minecraft.alien.network.mcpe.listener.McpeConnection;
import io.minimum.minecraft.alien.network.mcpe.packet.*;

class ResourcePackPacketHandler implements McpePacketHandler {
    private final McpeConnection connection;

    ResourcePackPacketHandler(McpeConnection connection) {
        this.connection = connection;
    }

    void initialize() {
        connection.write(new McpePlayStatus(McpePlayStatus.SUCCESS));
        connection.write(new McpeResourcePacks());
    }

    @Override
    public void connected() {

    }

    @Override
    public void disconnected() {

    }

    @Override
    public void handle(McpeResourcePackResponse packet) {
        if (packet.getStatus() == McpeResourcePackResponse.HAVE_PACKS) {
            connection.write(new McpeResourcePackStack());
        } else if (packet.getStatus() == McpeResourcePackResponse.COMPLETED || packet.getStatus() == McpeResourcePackResponse.REJECTED){
            connection.close("Resource pack testing");
        }
    }
}
