package io.minimum.minecraft.alien.network.mcpe.codec;

import io.minimum.minecraft.alien.network.mcpe.packet.McpePacket;
import io.minimum.minecraft.alien.network.mcpe.util.Varints;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.logging.log4j.LogManager;

import java.util.List;

public class McpeConnectionCodec extends MessageToMessageCodec<ByteBuf, McpePacket> {
    private final McpePacketRegistry registry;

    public McpeConnectionCodec(McpePacketRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, McpePacket msg, List<Object> out) throws Exception {
        int id = registry.getId(msg.getClass());
        if (id == Integer.MIN_VALUE) {
            throw new IllegalArgumentException("Message " + msg.getClass() + " does not have a registered ID");
        }

        ByteBuf buf = ctx.alloc().directBuffer();
        try {
            LogManager.getLogger().info("MCPE Encoder");

            Varints.encodeUnsigned(buf, id);
            msg.encode(buf);
            out.add(buf);
        } catch (Exception e) {
            buf.release();
            throw e;
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int id = (int) Varints.decodeUnsigned(msg);
        McpePacket packet = registry.supply(id);
        if (packet == null) {
            // just forward on this message
            out.add(msg.retain());
        } else {
            packet.decode(msg);
            out.add(packet);
        }
    }
}
