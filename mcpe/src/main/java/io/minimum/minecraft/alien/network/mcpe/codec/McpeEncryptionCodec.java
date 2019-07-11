package io.minimum.minecraft.alien.network.mcpe.codec;

import io.minimum.minecraft.alien.natives.encryption.VelocityCipher;
import io.minimum.minecraft.alien.natives.hash.AlienHash;
import io.minimum.minecraft.alien.natives.util.Natives;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class McpeEncryptionCodec extends MessageToMessageCodec<ByteBuf, ByteBuf> {
    private final VelocityCipher decrypt;
    private final VelocityCipher encrypt;
    private final AlienHash hash;
    private final ByteBuf key;
    private long counter;

    public McpeEncryptionCodec(byte[] key) throws GeneralSecurityException {
        this.key = PooledByteBufAllocator.DEFAULT.directBuffer().writeBytes(key);
        byte[] iv = Arrays.copyOf(key, 16);
        this.decrypt = Natives.cipher.get().forDecryption(new SecretKeySpec(key, "AES"), iv);
        this.encrypt = Natives.cipher.get().forEncryption(new SecretKeySpec(key, "AES"), iv);
        this.hash = Natives.hash.get().get();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.decrypt.dispose();
        this.encrypt.dispose();
        this.hash.dispose();
        this.key.release();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ByteBuf trailer = trailer(ctx.alloc(), msg);
        ByteBuf toEncrypt = ctx.alloc().directBuffer();
        try {
            toEncrypt.writeBytes(msg);
            toEncrypt.writeBytes(trailer);

            ByteBuf encrypted = this.encrypt.process(ctx, toEncrypt);
            out.add(encrypted);
        } finally {
            trailer.release();
            toEncrypt.release();
        }
    }

    private ByteBuf trailer(ByteBufAllocator alloc, ByteBuf msg) {
        ByteBuf counterBuf = alloc.directBuffer(8);
        counterBuf.writeLongLE(counter++);
        try {
            hash.update(counterBuf);
            hash.update(msg);
            hash.update(key);
            return hash.digest().slice(0, 8);
        } finally {
            counterBuf.release();
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ByteBuf decrypted = this.decrypt.process(ctx, msg);

        // Skip the trailer
        out.add(decrypted.slice(decrypted.readerIndex(), decrypted.readableBytes() - 8));
    }
}
