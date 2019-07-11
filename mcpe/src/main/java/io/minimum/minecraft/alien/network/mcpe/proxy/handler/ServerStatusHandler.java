package io.minimum.minecraft.alien.network.mcpe.proxy.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import network.ycc.raknet.RakNet;
import network.ycc.raknet.config.DefaultConfig;
import network.ycc.raknet.packet.Packet;
import network.ycc.raknet.packet.UnconnectedPing;
import network.ycc.raknet.packet.UnconnectedPong;
import network.ycc.raknet.server.pipeline.UdpPacketHandler;

import java.net.InetSocketAddress;

public class ServerStatusHandler extends UdpPacketHandler<UnconnectedPing> {
    public ServerStatusHandler() {
        super(UnconnectedPing.class);
    }

    @Override
    protected void handle(ChannelHandlerContext ctx, InetSocketAddress sender, UnconnectedPing packet) {
        System.out.println("Inbound ping: " + packet);

        UnconnectedPong pong = new UnconnectedPong();
        pong.setClientTime(packet.getClientTime());
        pong.setMagic(DefaultConfig.DEFAULT_MAGIC);
        pong.setServerId(0xdeadf001L);
        pong.setInfo("MCPE;Alien server;105;1.0.5;0;9000");
        respond(ctx, sender, pong);
    }

    private void respond(ChannelHandlerContext ctx, InetSocketAddress sender, Packet packet) {
        ByteBuf buf = ctx.alloc().ioBuffer(packet.sizeHint());
        try {
            RakNet.config(ctx).getCodec().encode(packet, buf);
            ctx.writeAndFlush(new DatagramPacket(buf, sender), ctx.voidPromise());
        } catch (Exception e) {
            buf.release();
            throw e;
        }
    }
}
