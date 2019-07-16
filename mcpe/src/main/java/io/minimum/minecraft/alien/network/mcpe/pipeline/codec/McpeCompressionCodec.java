package io.minimum.minecraft.alien.network.mcpe.pipeline.codec;

import io.minimum.minecraft.alien.natives.compression.VelocityCompressor;
import io.minimum.minecraft.alien.natives.util.Natives;
import io.minimum.minecraft.alien.network.mcpe.util.Varints;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import network.ycc.raknet.pipeline.FlushTickHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;

public class McpeCompressionCodec extends ChannelDuplexHandler {
    private static final Logger LOGGER = LogManager.getLogger(McpeCompressionCodec.class);

    private static final int COMPRESSED_PACKET_LIMIT = 2 * 1024 * 1024;
    private static final int PACKET_LIMIT = 10;

    private VelocityCompressor compressor;
    private ByteBuf holdingBuffer;
    private int queuedPackets = 0;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.compressor = Natives.compress.get().create(Deflater.DEFAULT_COMPRESSION);
        this.holdingBuffer = ctx.alloc().directBuffer();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.compressor.dispose();
        this.holdingBuffer.release();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            try {
                Varints.encodeUnsigned(holdingBuffer, buf.readableBytes());
                holdingBuffer.writeBytes(buf);
                queuedPackets++;

                if (queuedPackets >= PACKET_LIMIT) {
                    flushAllData(ctx);
                }

                FlushTickHandler.checkFlushTick(ctx.channel());
                promise.setSuccess();
            } catch (Exception e) {
                promise.setFailure(e);
            } finally {
                buf.release();
            }
        } else {
            super.write(ctx, msg, promise);
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        if (holdingBuffer.isReadable()) {
            flushAllData(ctx);
        }
        super.flush(ctx);
    }

    private void flushAllData(ChannelHandlerContext ctx) throws DataFormatException {
        ByteBuf compressed = ctx.alloc().directBuffer();
        try {
            compressor.deflate(holdingBuffer, compressed);
            LOGGER.debug("Flushed {} packets ({} => {} bytes)", queuedPackets, holdingBuffer.writerIndex(),
                    compressed.writerIndex());

            ctx.write(compressed, ctx.voidPromise());
            holdingBuffer.clear();
            queuedPackets = 0;
        } catch (Exception e) {
            compressed.release();
            throw e;
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            try {
                ByteBuf decompressed = ctx.alloc().directBuffer();
                try {
                    compressor.inflate(buf, decompressed, COMPRESSED_PACKET_LIMIT);

                    // Now frame the packets
                    while (decompressed.isReadable()) {
                        int length = (int) Varints.decodeUnsigned(decompressed);
                        ByteBuf packet = decompressed.readRetainedSlice(length);
                        ctx.fireChannelRead(packet);
                    }
                } catch (Exception e) {
                    for (int i = 0; i < decompressed.refCnt() - 1; i++) {
                        decompressed.release();
                    }
                    throw e;
                } finally {
                    // If there was at least one packet found, it would be retained.
                    decompressed.release();
                }
            } finally {
                buf.release();
            }
        } else {
            super.channelRead(ctx, msg);
        }
    }
}
