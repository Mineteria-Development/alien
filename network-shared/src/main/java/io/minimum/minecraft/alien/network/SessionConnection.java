package io.minimum.minecraft.alien.network;

import java.net.InetSocketAddress;

/**
 * Handles a session connection.
 */
public interface SessionConnection {
    /**
     * Returns the remote address for this connection.
     *
     * @return the remote address
     */
    InetSocketAddress getRemoteAddress();

    /**
     * Closes this connection. No further packets can be received or sent after this function is called.
     */
    void close();

    /**
     * Returns whether or not the session has been closed.
     *
     * @return whether or not the session has been closed
     */
    boolean isClosed();

    /**
     * Called every 50 milliseconds from the session object.
     */
    void tick();

    void sendPacket(Object object);
}
