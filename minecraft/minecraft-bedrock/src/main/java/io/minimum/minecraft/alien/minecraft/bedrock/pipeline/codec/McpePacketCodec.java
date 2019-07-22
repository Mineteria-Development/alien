package io.minimum.minecraft.alien.minecraft.bedrock.pipeline.codec;

import io.minimum.minecraft.alien.minecraft.bedrock.packet.McpePacket;
import io.minimum.minecraft.alien.minecraft.bedrock.packet.ProtocolVersions;
import io.minimum.minecraft.alien.minecraft.shared.codec.Varints;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

public class McpePacketCodec extends MessageToMessageCodec<ByteBuf, McpePacket> {
    private McpePacketRegistry registry;

    public McpePacketCodec(McpePacketRegistry registry) {
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
            Varints.encodeUnsigned(buf, id);
            msg.encode(buf, registry.getProtocolVersion());
            out.add(buf);
        } catch (Exception e) {
            buf.release();
            throw e;
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int ri = msg.readerIndex();
        int id = (int) Varints.decodeUnsigned(msg);
        McpePacket packet = registry.supply(id);
        if (packet == null) {
            // just forward on this message
            msg.readerIndex(ri);
            out.add(msg.retain());
        } else {
            packet.decode(msg, registry.getProtocolVersion());
            if (msg.isReadable()) {
                System.out.println("Incomplete message! " + packet);
                throw new CorruptedFrameException("Did not read all bytes for message " + packet.getClass().getName() + ": " + msg);
            }
            out.add(packet);
        }
    }

    public void setRegistry(McpePacketRegistry registry) {
        this.registry = registry;
    }
}
