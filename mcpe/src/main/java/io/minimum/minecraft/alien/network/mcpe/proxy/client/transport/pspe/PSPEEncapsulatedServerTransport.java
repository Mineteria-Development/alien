package io.minimum.minecraft.alien.network.mcpe.proxy.client.transport.pspe;

import io.minimum.minecraft.alien.network.mcpe.pipeline.McpeConnection;
import io.minimum.minecraft.alien.network.mcpe.pipeline.PipelineUtils;
import io.minimum.minecraft.alien.network.mcpe.proxy.client.transport.ServerTransport;
import io.minimum.minecraft.alien.network.mcpe.proxy.client.transport.pspe.codec.EncapsulationInfoEncoder;
import io.minimum.minecraft.alien.network.mcpe.proxy.client.transport.pspe.codec.MinecraftVarintFrameDecoder;
import io.minimum.minecraft.alien.network.mcpe.proxy.client.transport.pspe.codec.MinecraftVarintLengthEncoder;
import io.minimum.minecraft.alien.network.util.TransportType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.concurrent.Promise;
import network.ycc.raknet.pipeline.FlushTickHandler;

import java.net.InetSocketAddress;

/**
 * Implements a transport intended to allow server connections via the ProtocolSupport PE encapsulation protocol.
 */
public class PSPEEncapsulatedServerTransport implements ServerTransport {
    private final Bootstrap base;

    public PSPEEncapsulatedServerTransport(TransportType transportType) {
        this.base = new Bootstrap()
                .channel(transportType.getSocketChannelClass())
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        // Must be added before anything else, since this is a custom-formatted packet
                        ch.pipeline().addLast("pspe-encapsulation-encoding", EncapsulationInfoEncoder.INSTANCE);

                        // We have to do explicit flushing, too. A little hack where we also have write() do a flush
                        // sometimes.
                        ch.pipeline().addLast(FlushTickHandler.NAME, new FlushTickHandler() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                super.write(ctx, msg, promise);
                                maybeFlush(ch);
                            }
                        });

                        // Framing
                        ch.pipeline().addLast("pspe-frame-decoder", new MinecraftVarintFrameDecoder());
                        ch.pipeline().addLast("pspe-frame-encoder", MinecraftVarintLengthEncoder.INSTANCE);
                    }
                });
    }

    @Override
    public Promise<McpeConnection> open(McpeConnection establisher, InetSocketAddress target) {
        Promise<McpeConnection> promise = establisher.eventLoop().newPromise();
        base.clone(establisher.eventLoop())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .connect(target)
                .addListener(((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        System.out.println("PSPE Enroute to " + target);
                        Channel ch = future.channel();

                        ch.writeAndFlush(new EncapsulationInfo(false, (InetSocketAddress) establisher.getRemoteAddress()));

                        promise.setSuccess(PipelineUtils.initializeClientPipeline(ch, establisher));
                    } else {
                        promise.setFailure(future.cause());
                    }
                }));
        return promise;
    }
}
