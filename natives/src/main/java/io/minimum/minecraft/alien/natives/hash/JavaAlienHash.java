package io.minimum.minecraft.alien.natives.hash;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JavaAlienHash implements AlienHash {
    private final MessageDigest digest;

    public JavaAlienHash() {
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public void update(ByteBuf buf) {
        digest.update(ByteBufUtil.getBytes(buf));
    }

    @Override
    public ByteBuf digest() {
        byte[] result = digest.digest();
        digest.reset();
        return Unpooled.wrappedBuffer(result);
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isNative() {
        return false;
    }
}
