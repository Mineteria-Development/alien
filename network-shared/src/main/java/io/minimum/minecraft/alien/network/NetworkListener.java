package io.minimum.minecraft.alien.network;

import io.minimum.minecraft.alien.network.util.TransportType;
import io.netty.channel.EventLoopGroup;

public interface NetworkListener {
    boolean bind(TransportType type, EventLoopGroup boss, EventLoopGroup worker);

    void close();
}
