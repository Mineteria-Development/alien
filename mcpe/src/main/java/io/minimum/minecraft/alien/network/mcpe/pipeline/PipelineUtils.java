package io.minimum.minecraft.alien.network.mcpe.pipeline;

import io.minimum.minecraft.alien.network.mcpe.packet.ProtocolVersions;
import io.minimum.minecraft.alien.network.mcpe.pipeline.codec.McpePacketCodec;
import io.netty.channel.Channel;

public class PipelineUtils {
    private PipelineUtils() {
        throw new AssertionError();
    }

    public static McpeConnection initializeClientPipeline(Channel ch, McpeConnection establishingClient) {
        // Decode/encode MCPE packets
        ch.pipeline().addLast("alien-mcpe-codec", new McpePacketCodec(ProtocolVersions.PE_1_11));

        // Handle MCPE packets
        McpeConnection mc = new McpeConnection(ch);
        ch.pipeline().addLast("alien-mcpe", mc);
        return mc;
    }
}
