package io.minimum.minecraft.alien.network.mcpe.proxy.client.transport;

import io.minimum.minecraft.alien.network.mcpe.listener.McpeConnection;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

public interface ServerTransport {
    Promise<McpeConnection> open(McpeConnection establisher, InetSocketAddress target);
}
