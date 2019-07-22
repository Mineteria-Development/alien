package io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.transport;

import io.minimum.minecraft.alien.minecraft.bedrock.pipeline.BedrockConnection;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

/**
 * A server transport opens the connection to the remote server. The connection established is ready for sending the
 * {@link io.minimum.minecraft.alien.minecraft.bedrock.packet.McpeLogin} packet.
 */
public interface ServerTransport {
    /**
     * Opens a connection to the remote server.
     * @param establisher the connection that is establishing the connection
     * @param target the remote address of the server
     * @return a Promise on the {@code establisher}'s event loop
     */
    Promise<BedrockConnection> open(BedrockConnection establisher, InetSocketAddress target);
}
