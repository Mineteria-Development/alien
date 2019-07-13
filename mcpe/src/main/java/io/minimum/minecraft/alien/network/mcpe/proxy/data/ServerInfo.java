package io.minimum.minecraft.alien.network.mcpe.proxy.data;

import java.net.InetSocketAddress;

public class ServerInfo {
    private final String name;
    private final InetSocketAddress destination;

    public ServerInfo(String name, InetSocketAddress destination) {
        this.name = name;
        this.destination = destination;
    }

    public String getName() {
        return name;
    }

    public InetSocketAddress getDestination() {
        return destination;
    }
}
