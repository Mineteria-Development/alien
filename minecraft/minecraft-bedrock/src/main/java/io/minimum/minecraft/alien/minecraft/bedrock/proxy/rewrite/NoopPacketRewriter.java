package io.minimum.minecraft.alien.minecraft.bedrock.proxy.rewrite;

import io.netty.buffer.ByteBuf;

public class NoopPacketRewriter implements PacketRewriter {
    @Override
    public ByteBuf rewrite(ByteBuf in) {
        return in.retain();
    }

    @Override
    public int getClientEntityId() {
        throw new UnsupportedOperationException("This rewriter doesn't rewrite anything!");
    }

    @Override
    public int getServerEntityId() {
        throw new UnsupportedOperationException("This rewriter doesn't rewrite anything!");
    }

    @Override
    public void setServerEntityId(int id) {
        throw new UnsupportedOperationException("This rewriter doesn't rewrite anything!");
    }
}
