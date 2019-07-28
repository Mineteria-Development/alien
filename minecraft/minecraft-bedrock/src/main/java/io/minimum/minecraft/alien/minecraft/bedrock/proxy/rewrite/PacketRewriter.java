package io.minimum.minecraft.alien.minecraft.bedrock.proxy.rewrite;

import io.netty.buffer.ByteBuf;

public interface PacketRewriter {
    ByteBuf rewrite(ByteBuf in);

    int getClientEntityId();

    int getServerEntityId();

    void setServerEntityId(int id);
}
