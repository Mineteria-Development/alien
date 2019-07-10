package io.minimum.minecraft.alien.network.mcpe.codec;

import io.minimum.minecraft.alien.natives.compression.VelocityCompressor;
import io.minimum.minecraft.alien.natives.util.Natives;
import io.minimum.minecraft.alien.network.mcpe.util.Varints;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;
import java.util.zip.Deflater;

public class McpeCompressionCodec extends MessageToMessageCodec<ByteBuf, ByteBuf> {
    private static final int MAX_COMPRESSED_SIZE = 2 * 1024 * 1024;

    private VelocityCompressor compressor;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.compressor = Natives.compress.get().create(Deflater.DEFAULT_COMPRESSION);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.compressor.dispose();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ByteBuf buf = ctx.alloc().directBuffer();
        Varints.encodeSigned(buf, msg.readableBytes());
        try {
            compressor.deflate(msg, buf);
            out.add(buf);
        } catch (Exception e) {
            buf.release();
            throw e;
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        ByteBuf decompressed = ctx.alloc().directBuffer();
        try {
            compressor.inflate(msg, decompressed, MAX_COMPRESSED_SIZE);

            // Now frame the packets
            while (decompressed.isReadable()) {
                int length = (int) Varints.decodeUnsigned(decompressed);
                ByteBuf packet = decompressed.readRetainedSlice(length);
                out.add(packet);
            }
        } catch (Exception e) {
            decompressed.release();
            throw e;
        }
    }
}
