package io.minimum.minecraft.alien.network.mcpe.proxy.client.transport;

import io.minimum.minecraft.alien.network.mcpe.listener.McpeConnection;
import io.minimum.minecraft.alien.network.mcpe.pipeline.PipelineUtils;
import io.minimum.minecraft.alien.network.mcpe.pipeline.codec.McpeCompressionCodec;
import io.minimum.minecraft.alien.network.util.TransportType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.Promise;
import network.ycc.raknet.client.channel.RakNetClientChannel;
import network.ycc.raknet.pipeline.UserDataCodec;

import java.net.InetSocketAddress;

public class RakNetServerTransport implements ServerTransport {
    private final Bootstrap base;

    public RakNetServerTransport(TransportType transportType) {
        this.base = new Bootstrap()
                .channelFactory(() -> new RakNetClientChannel(transportType.getDatagramChannelClass()));
    }

    @Override
    public Promise<McpeConnection> open(McpeConnection establisher, InetSocketAddress target) {
        Promise<McpeConnection> promise = establisher.eventLoop().newPromise();
        base.clone(establisher.eventLoop())
                .connect(target)
                .addListener(((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        Channel ch = future.channel();

                        // User data packet
                        ch.pipeline().addLast(UserDataCodec.NAME, new UserDataCodec(0xFE));

                        // We're going over the regular RakNet transport, and that means we need to enable compression.
                        ch.pipeline().addLast("alien-compression", new McpeCompressionCodec());

                        promise.setSuccess(PipelineUtils.initializeClientPipeline(ch, establisher));
                    } else {
                        promise.setFailure(future.cause());
                    }
                }));
        return promise;
    }
}
