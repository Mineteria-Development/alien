package io.minimum.minecraft.alien.network.mcpe.listener;

import io.minimum.minecraft.alien.network.NetworkListener;
import io.minimum.minecraft.alien.network.mcpe.pipeline.codec.DatagramPacketAbsorber;
import io.minimum.minecraft.alien.network.mcpe.pipeline.codec.McpeCompressionCodec;
import io.minimum.minecraft.alien.network.mcpe.pipeline.codec.McpeConnectionCodec;
import io.minimum.minecraft.alien.network.mcpe.pipeline.codec.McpePacketRegistry;
import io.minimum.minecraft.alien.network.mcpe.packet.*;
import io.minimum.minecraft.alien.network.mcpe.proxy.player.handler.InitialNetworkPacketHandler;
import io.minimum.minecraft.alien.network.mcpe.proxy.player.handler.ServerStatusHandler;
import io.minimum.minecraft.alien.network.util.TransportType;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import network.ycc.raknet.RakNet;
import network.ycc.raknet.pipeline.UserDataCodec;
import network.ycc.raknet.server.channel.RakNetServerChannel;

import java.net.InetSocketAddress;

public class McpeOverYesdogRakNetNetworkListener implements NetworkListener {
    private final InetSocketAddress bound;
    private Channel channel;

    public McpeOverYesdogRakNetNetworkListener(InetSocketAddress bound) {
        this.bound = bound;
    }

    @Override
    public boolean bind(TransportType type, EventLoopGroup boss, EventLoopGroup worker) {
        final McpePacketRegistry registry = ProtocolVersions.PE_1_11; // TODO: Let's do better than this

        this.channel = new ServerBootstrap()
                .group(boss, worker) // NB: this is fairly useless if we do SO_REUSEPORT
                .channelFactory(() -> new RakNetServerChannel(type.getDatagramChannelClass()))
                .option(RakNet.SERVER_ID, 0xdeadf001L)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast("server-status", new ServerStatusHandler());

                        ch.eventLoop().execute(() -> {
                            ch.pipeline().addLast("datagram-absorber", new DatagramPacketAbsorber());
                        });
                    }
                })
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        // Get the MCPE user data packet too
                        ch.pipeline().addLast(UserDataCodec.NAME, new UserDataCodec(0xFE));

                        // The client sends compressed packets immediately, so we need to inject a compressor immediately
                        ch.pipeline().addLast("alien-compression", new McpeCompressionCodec());

                        // Decode/encode MCPE packets
                        ch.pipeline().addLast("alien-mcpe-codec", new McpeConnectionCodec(registry));

                        // Handle MCPE packets
                        McpeConnection mc = new McpeConnection(ch, null);
                        mc.setPacketHandler(new InitialNetworkPacketHandler(mc));
                        ch.pipeline().addLast("alien-mcpe", mc);
                    }
                }).bind(bound).syncUninterruptibly().channel();

        System.out.println("Server ought to be up now");
        return channel != null;
    }

    @Override
    public void close() {
        if (this.channel != null) {
            channel.close().syncUninterruptibly();
        }
    }
}
