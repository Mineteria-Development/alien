package io.minimum.minecraft.alien.network.mcpe.pipeline;

import com.google.common.base.Preconditions;
import io.minimum.minecraft.alien.network.mcpe.pipeline.codec.McpeEncryptionCodec;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeDisconnect;
import io.minimum.minecraft.alien.network.mcpe.packet.McpePacket;
import io.minimum.minecraft.alien.network.mcpe.packet.McpePacketHandler;
import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.ReferenceCountUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.net.SocketAddress;
import java.security.GeneralSecurityException;

public class McpeConnection extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LogManager.getLogger(McpeConnection.class);

    private final Channel channel;
    private SocketAddress remoteAddress;
    private @Nullable McpePacketHandler sessionHandler;
    private @Nullable MinecraftConnectionAssociation association;
    private int protocolVersion;
    private boolean knownDisconnect = false;

    /**
     * Initializes a new {@link McpeConnection} instance.
     * @param channel the channel on the connection
     */
    public McpeConnection(Channel channel) {
        this.channel = channel;
        this.remoteAddress = channel.remoteAddress();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (sessionHandler != null) {
            sessionHandler.connected();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (sessionHandler != null) {
            sessionHandler.disconnected();
        }

        if (association != null && !knownDisconnect) {
            logger.info("{} has disconnected", association);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            logger.info("Got incoming message {} from {}", msg, remoteAddress);

            if (sessionHandler == null) {
                // No session handler available, do nothing
                return;
            }

            if (msg instanceof McpePacket && ((McpePacket) msg).handle(sessionHandler)) {
                return;
            }

            sessionHandler.handleGeneric(msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (ctx.channel().isActive()) {
            if (sessionHandler != null) {
                try {
                    sessionHandler.exception(cause);
                } catch (Exception ex) {
                    logger.error("{}: exception handling exception", (association != null ? association :
                            channel.remoteAddress()), cause);
                }
            }

            if (cause instanceof ReadTimeoutException) {
                logger.error("{}: read timed out", remoteAddress);
            } else {
                logger.error("{}: exception encountered", remoteAddress, cause);
            }

            ctx.close();
        }
    }

    public EventLoop eventLoop() {
        return channel.eventLoop();
    }

    /**
     * Writes a message to the connection.
     * @param msg the message to write
     */
    public void write(Object msg) {
        if (channel.isActive()) {
            logger.info("Writing {} to {}", msg, remoteAddress);
            channel.write(msg, channel.voidPromise());
        }
    }

    /**
     * Writes and immediately flushes a message to the connection, invoking the specified channel listeners.
     * @param msg the message to write
     * @param listeners the listeners
     */
    public void write(Object msg, ChannelFutureListener... listeners) {
        if (channel.isActive()) {
            logger.info("Writing {} to {}", msg, remoteAddress);
            channel.write(msg).addListeners(listeners);
        }
    }

    /**
     * Closes the connection after disconnecting with the message.
     * @param msg the message to write
     */
    public void closeWith(String msg) {
        if (channel.isActive()) {
            knownDisconnect = true;
            channel.writeAndFlush(new McpeDisconnect(msg)).addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * Immediately closes the connection.
     */
    public void close() {
        if (channel.isActive()) {
            channel.close();
        }
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isClosed() {
        return !channel.isActive();
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    /*
    /**
     * Sets the new protocol version for the connection.
     * @param protocolVersion the protocol version to use
     */
    /*
    public void setProtocolVersion(int protocolVersion) {
        ProtocolRegistry registry = BetaPackets.VERSIONS.getProtocolRegistry(protocolVersion);
        if (registry == null) {
            throw new IllegalArgumentException("Unknown protocol version " + protocolVersion);
        }

        this.protocolVersion = protocolVersion;
        this.channel.pipeline().get(MinecraftBetaDecoder.class).setRegistry(registry);
        this.channel.pipeline().get(MinecraftBetaEncoder.class).setRegistry(registry);
    }*/

    public @Nullable McpePacketHandler getSessionHandler() {
        return sessionHandler;
    }

    /**
     * Sets the session handler for this connection.
     * @param sessionHandler the handler to use
     */
    public void setSessionHandler(McpePacketHandler sessionHandler) {
        if (this.sessionHandler != null) {
            this.sessionHandler.deactivated();
        }
        this.sessionHandler = sessionHandler;
        sessionHandler.activated();
    }

    private void ensureOpen() {
        Preconditions.checkState(!isClosed(), "Connection is closed.");
    }

    public @Nullable MinecraftConnectionAssociation getAssociation() {
        return association;
    }

    public void setAssociation(MinecraftConnectionAssociation association) {
        this.association = association;
    }

    public void enableEncryption(byte[] serverKey) throws GeneralSecurityException {
        McpeEncryptionCodec encryptionCodec = new McpeEncryptionCodec(serverKey);
        channel.pipeline().addBefore("alien-compression", "alien-encryption", encryptionCodec);
    }
}
