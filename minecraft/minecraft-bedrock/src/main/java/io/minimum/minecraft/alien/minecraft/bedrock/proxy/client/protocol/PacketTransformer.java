package io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.protocol;

import io.netty.buffer.ByteBuf;

/**
 * Transforms packets from their original form to a different form. Used to accomplish entity ID rewriting.
 */
public interface PacketTransformer {
    void transform(ByteBuf buf);
}
