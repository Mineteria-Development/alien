package io.minimum.minecraft.alien.network.mcpe.listener;

import com.google.common.base.Preconditions;
import io.minimum.minecraft.alien.network.mcpe.packet.McpePacket;
import io.minimum.minecraft.alien.network.mcpe.packet.McpePacketHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.SocketAddress;

public class McpeConnection extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger(McpeConnection.class);

    private final Channel channel;
    private McpePacketHandler packetHandler;

    McpeConnection(Channel channel, McpePacketHandler packetHandler) {
        this.channel = channel;
        this.packetHandler = packetHandler;
    }

    public void close() {
        Preconditions.checkState(channel.isOpen(), "Backend channel is not open.");
        channel.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            LOGGER.info("Received message {}", msg);

            if (packetHandler != null) {
                if (msg instanceof McpePacket) {
                    ((McpePacket) msg).handle(packetHandler);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    public McpePacketHandler getPacketHandler() {
        return packetHandler;
    }

    public void setPacketHandler(McpePacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }
}
