package io.minimum.minecraft.alien.network.mcpe.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

public class DatagramPacketAbsorber extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DatagramPacket) {
            ((DatagramPacket) msg).release();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
