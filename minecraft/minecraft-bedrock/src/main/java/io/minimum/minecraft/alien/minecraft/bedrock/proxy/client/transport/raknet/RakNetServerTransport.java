package io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.transport.raknet;

import io.minimum.minecraft.alien.minecraft.bedrock.pipeline.BedrockConnection;
import io.minimum.minecraft.alien.minecraft.bedrock.pipeline.PipelineUtils;
import io.minimum.minecraft.alien.minecraft.bedrock.pipeline.codec.McpeCompressionCodec;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.transport.ServerTransport;
import io.minimum.minecraft.alien.network.util.TransportType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.util.concurrent.Promise;
import network.ycc.raknet.client.channel.RakNetClientChannel;
import network.ycc.raknet.pipeline.UserDataCodec;

import java.net.InetSocketAddress;

/**
 * Implements a server transport for RakNet-based servers, such as PMMP, NukkitX, MiNET, the vanilla dedicated server,
 * and PSPE without the encapsulation protocol active.
 */
public class RakNetServerTransport implements ServerTransport {
    private final Bootstrap base;

    public RakNetServerTransport(TransportType transportType) {
        this.base = new Bootstrap()
                .channelFactory(() -> new RakNetClientChannel(transportType.getDatagramChannelClass()));
    }

    @Override
    public Promise<BedrockConnection> open(BedrockConnection establisher, InetSocketAddress target) {
        Promise<BedrockConnection> promise = establisher.eventLoop().newPromise();
        base.clone(establisher.eventLoop())
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        // User data packet
                        ch.pipeline().addLast(UserDataCodec.NAME, new UserDataCodec(0xFE));

                        // We're going over the regular RakNet transport, and that means we need to enable compression.
                        ch.pipeline().addLast("alien-compression", new McpeCompressionCodec());
                    }
                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .connect(target)
                .addListener(((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        System.out.println("Enroute to " + target);
                        Channel ch = future.channel();
                        promise.setSuccess(PipelineUtils.initializeClientPipeline(ch, establisher));
                    } else {
                        promise.setFailure(future.cause());
                    }
                }));
        return promise;
    }
}
