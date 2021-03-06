package io.minimum.minecraft.alien;

import io.minimum.minecraft.alien.minecraft.bedrock.listener.BedrockProxyNetworkListener;
import io.minimum.minecraft.alien.network.util.TransportType;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

public class Alien {
    private static final Logger LOGGER = LogManager.getLogger(Alien.class);

    public static void main(String... args) throws InterruptedException, UnknownHostException {
        if (!isJCEUnlimitedStrength()) {
            // barf
            LOGGER.error("You don't have unlimited strength JCE policies installed.");
            System.exit(1);
        }
        boolean canUseSoReusePort = Epoll.isAvailable();
        if (!canUseSoReusePort) {
            LOGGER.warn("Your OS does not support SO_REUSEPORT. This means your proxy will use just one thread to");
            LOGGER.warn("handle inbound and outbound connections. This is fine for a small number of players, but");
            LOGGER.warn("will inevitably result in scalability bottlenecks. As a result, you should ONLY run Alien");
            LOGGER.warn("on a recent Linux distribution, so it can use SO_REUSEPORT.");
        }

        TransportType type = TransportType.bestType();
        EventLoopGroup boss = type.createEventLoopGroup(TransportType.Type.BOSS);
        EventLoopGroup worker = type.createEventLoopGroup(TransportType.Type.WORKER);

        new BedrockProxyNetworkListener(new InetSocketAddress("127.0.0.1", 19132)).bind(type, boss, worker);

        while (true) {
            Thread.sleep(1000);
        }
    }

    public static boolean isJCEUnlimitedStrength() {
        try {
            return Cipher.getMaxAllowedKeyLength("AES") == Integer.MAX_VALUE;
        } catch (NoSuchAlgorithmException e) {
            // AES should always exist.
            throw new AssertionError(e);
        }
    }
}
