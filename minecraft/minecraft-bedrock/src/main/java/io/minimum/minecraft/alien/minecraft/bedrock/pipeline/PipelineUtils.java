package io.minimum.minecraft.alien.minecraft.bedrock.pipeline;

import io.minimum.minecraft.alien.minecraft.bedrock.pipeline.codec.McpePacketCodec;
import io.netty.channel.Channel;

public class PipelineUtils {
    private PipelineUtils() {
        throw new AssertionError();
    }

    public static BedrockConnection initializeClientPipeline(Channel ch, BedrockConnection establishingClient) {
        // Decode/encode MCPE packets
        ch.pipeline().addLast("alien-mcpe-codec", new McpePacketCodec(establishingClient.getPacketRegistry()));

        // Handle MCPE packets
        BedrockConnection mc = new BedrockConnection(ch, establishingClient.getPacketRegistry());
        ch.pipeline().addLast("alien-mcpe", mc);
        return mc;
    }
}
