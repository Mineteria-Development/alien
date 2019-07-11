package io.minimum.minecraft.alien.natives.encryption;

import io.minimum.minecraft.alien.natives.Disposable;
import io.minimum.minecraft.alien.natives.Native;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import javax.crypto.ShortBufferException;

public interface VelocityCipher extends Disposable, Native {

    void process(ByteBuf source, ByteBuf destination) throws ShortBufferException;

    ByteBuf process(ChannelHandlerContext ctx, ByteBuf source) throws ShortBufferException;
}
