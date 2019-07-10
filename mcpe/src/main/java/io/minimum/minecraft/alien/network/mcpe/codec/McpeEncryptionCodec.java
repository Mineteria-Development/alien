package io.minimum.minecraft.alien.network.mcpe.codec;

import io.minimum.minecraft.alien.natives.encryption.VelocityCipher;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

public class McpeEncryptionCodec extends MessageToMessageCodec<ByteBuf, ByteBuf> {
    private final VelocityCipher cipher;

    public McpeEncryptionCodec(VelocityCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.cipher.dispose();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {

    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {

    }
}
