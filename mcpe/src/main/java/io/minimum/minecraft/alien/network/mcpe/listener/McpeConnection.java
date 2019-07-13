package io.minimum.minecraft.alien.network.mcpe.listener;

import com.google.common.base.Preconditions;
import io.minimum.minecraft.alien.network.mcpe.pipeline.codec.McpeEncryptionCodec;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeDisconnect;
import io.minimum.minecraft.alien.network.mcpe.packet.McpePacket;
import io.minimum.minecraft.alien.network.mcpe.packet.McpePacketHandler;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;

public class McpeConnection extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger(McpeConnection.class);

    private final Channel channel;
    private McpePacketHandler packetHandler;

    public McpeConnection(Channel channel, McpePacketHandler packetHandler) {
        this.channel = channel;
        this.packetHandler = packetHandler;
    }

    public void close(String message) {
        Preconditions.checkState(channel.isOpen(), "Backend channel is not open.");
        channel.writeAndFlush(new McpeDisconnect(message)).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            LOGGER.info("Received message {}", msg);

            if (packetHandler != null) {
                if (msg instanceof McpePacket && ((McpePacket) msg).handle(packetHandler)) {
                    return;
                }

                packetHandler.handleGeneric(msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    public void write(Object packet) {
        channel.write(packet, channel.voidPromise());
    }

    public void write(Object packet, ChannelFutureListener listener) {
        channel.write(packet).addListener(listener);
    }

    public EventLoop eventLoop() {
        return channel.eventLoop();
    }

    public @Nullable McpePacketHandler getPacketHandler() {
        return packetHandler;
    }

    public void setPacketHandler(McpePacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    public SocketAddress getRemoteAddress() {
        return channel.remoteAddress();
    }

    public void enableEncryption(byte[] serverKey) throws GeneralSecurityException {
        McpeEncryptionCodec encryptionCodec = new McpeEncryptionCodec(serverKey);
        channel.pipeline().addBefore("alien-compression", "alien-encryption", encryptionCodec);
    }
}
