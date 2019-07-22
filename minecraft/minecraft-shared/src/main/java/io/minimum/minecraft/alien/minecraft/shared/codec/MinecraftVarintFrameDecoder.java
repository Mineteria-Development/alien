package io.minimum.minecraft.alien.minecraft.shared.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

// Copied from Velocity
public class MinecraftVarintFrameDecoder extends ByteToMessageDecoder {

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (!in.isReadable()) {
      return;
    }

    int origReaderIndex = in.readerIndex();
    for (int i = 0; i < 3; i++) {
      if (!in.isReadable()) {
        in.readerIndex(origReaderIndex);
        return;
      }

      byte read = in.readByte();
      if (read >= 0) {
        // Make sure reader index of length buffer is returned to the beginning
        in.readerIndex(origReaderIndex);
        int packetLength = (int) Varints.decodeUnsigned(in);
        if (packetLength == 0) {
          return;
        }

        if (in.readableBytes() < packetLength) {
          in.readerIndex(origReaderIndex);
          return;
        }

        out.add(in.readRetainedSlice(packetLength));
        return;
      }
    }

    throw new CorruptedFrameException("VarInt too big");
  }
}
