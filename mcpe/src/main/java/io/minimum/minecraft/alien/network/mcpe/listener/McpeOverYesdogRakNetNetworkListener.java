package io.minimum.minecraft.alien.network.mcpe.listener;

import io.minimum.minecraft.alien.network.NetworkListener;
import io.minimum.minecraft.alien.network.mcpe.codec.DatagramPacketAbsorber;
import io.minimum.minecraft.alien.network.mcpe.codec.McpeCompressionCodec;
import io.minimum.minecraft.alien.network.mcpe.codec.McpeConnectionCodec;
import io.minimum.minecraft.alien.network.mcpe.codec.McpePacketRegistry;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeClientToServerEncryptionHandshake;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeDisconnect;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeLogin;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeServerToClientEncryptionHandshake;
import io.minimum.minecraft.alien.network.mcpe.proxy.handler.InitialNetworkPacketHandler;
import io.minimum.minecraft.alien.network.mcpe.proxy.handler.ServerStatusHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import network.ycc.raknet.RakNet;
import network.ycc.raknet.pipeline.UserDataCodec;
import network.ycc.raknet.server.RakNetServer;

import java.net.InetSocketAddress;

public class McpeOverYesdogRakNetNetworkListener implements NetworkListener {
    private final InetSocketAddress bound;
    private Channel channel;

    public McpeOverYesdogRakNetNetworkListener(InetSocketAddress bound) {
        this.bound = bound;
    }

    @Override
    public boolean bind() {
        final McpePacketRegistry registry = new McpePacketRegistry();
        registry.register(0x01, McpeLogin.class, McpeLogin::new);
        registry.register(0x03, McpeServerToClientEncryptionHandshake.class, McpeServerToClientEncryptionHandshake::new);
        registry.register(0x04, McpeClientToServerEncryptionHandshake.class, McpeClientToServerEncryptionHandshake::new);
        registry.register(0x05, McpeDisconnect.class, McpeDisconnect::new);

        this.channel = new ServerBootstrap()
                .group(new NioEventLoopGroup(), new DefaultEventLoopGroup())
                .channel(RakNetServer.CHANNEL)
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
