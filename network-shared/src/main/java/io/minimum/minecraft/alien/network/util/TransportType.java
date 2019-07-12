package io.minimum.minecraft.alien.network.util;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.kqueue.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ThreadFactory;
import java.util.function.BiFunction;

public enum TransportType {
    NIO("NIO", NioServerSocketChannel.class, NioSocketChannel.class, NioDatagramChannel.class,
            (name, type) -> new NioEventLoopGroup(0, createThreadFactory(name, type))),
    EPOLL("epoll", EpollServerSocketChannel.class, EpollSocketChannel.class,
            EpollDatagramChannel.class,
            (name, type) -> new EpollEventLoopGroup(0, createThreadFactory(name, type))),
    KQUEUE("Kqueue", KQueueServerSocketChannel.class, KQueueSocketChannel.class,
            KQueueDatagramChannel.class,
            (name, type) -> new KQueueEventLoopGroup(0, createThreadFactory(name, type)));

    final String name;
    final Class<? extends ServerSocketChannel> serverSocketChannelClass;
    final Class<? extends SocketChannel> socketChannelClass;
    final Class<? extends DatagramChannel> datagramChannelClass;
    final BiFunction<String, Type, EventLoopGroup> eventLoopGroupFactory;

    TransportType(final String name,
                  final Class<? extends ServerSocketChannel> serverSocketChannelClass,
                  final Class<? extends SocketChannel> socketChannelClass,
                  final Class<? extends DatagramChannel> datagramChannelClass,
                  final BiFunction<String, Type, EventLoopGroup> eventLoopGroupFactory) {
        this.name = name;
        this.serverSocketChannelClass = serverSocketChannelClass;
        this.socketChannelClass = socketChannelClass;
        this.datagramChannelClass = datagramChannelClass;
        this.eventLoopGroupFactory = eventLoopGroupFactory;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public EventLoopGroup createEventLoopGroup(final Type type) {
        return this.eventLoopGroupFactory.apply(this.name, type);
    }

    private static ThreadFactory createThreadFactory(final String name, final Type type) {
        return new AlienNettyThreadFactory("Netty " + name + ' ' + type.toString() + " #%d");
    }

    public static TransportType bestType() {
        if (Epoll.isAvailable()) {
            return EPOLL;
        } else if (KQueue.isAvailable()) {
            return KQUEUE;
        } else {
            return NIO;
        }
    }

    public Class<? extends ServerSocketChannel> getServerSocketChannelClass() {
        return serverSocketChannelClass;
    }

    public Class<? extends SocketChannel> getSocketChannelClass() {
        return socketChannelClass;
    }

    public Class<? extends DatagramChannel> getDatagramChannelClass() {
        return datagramChannelClass;
    }

    public enum Type {
        BOSS("Boss"),
        WORKER("Worker");

        private final String name;

        Type(final String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
