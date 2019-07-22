package io.minimum.minecraft.alien.minecraft.bedrock.pipeline;

import io.minimum.minecraft.alien.minecraft.bedrock.packet.ProtocolVersions;
import io.minimum.minecraft.alien.minecraft.bedrock.pipeline.codec.McpePacketCodec;
import io.netty.channel.Channel;

public class PipelineUtils {
    private PipelineUtils() {
        throw new AssertionError();
    }

    public static BedrockConnection initializeClientPipeline(Channel ch, BedrockConnection establishingClient) {
        // Decode/encode MCPE packets
        ch.pipeline().addLast("alien-mcpe-codec", new McpePacketCodec(ProtocolVersions.getRegistry(ProtocolVersions.PE_1_11)));

        // Handle MCPE packets
        BedrockConnection mc = new BedrockConnection(ch);
        ch.pipeline().addLast("alien-mcpe", mc);
        return mc;
    }
}
