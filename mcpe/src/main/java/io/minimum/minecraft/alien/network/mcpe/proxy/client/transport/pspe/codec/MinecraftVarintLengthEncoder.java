package io.minimum.minecraft.alien.network.mcpe.proxy.client.transport.pspe.codec;

import io.minimum.minecraft.alien.network.mcpe.util.Varints;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

// Copied from Velocity
@ChannelHandler.Sharable
public class MinecraftVarintLengthEncoder extends MessageToMessageEncoder<ByteBuf> {

  public static final MinecraftVarintLengthEncoder INSTANCE = new MinecraftVarintLengthEncoder();

  private MinecraftVarintLengthEncoder() {
  }

  @Override
  protected void encode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list)
      throws Exception {
    ByteBuf lengthBuf = ctx.alloc().buffer(5); // the maximum size of a varint
    Varints.encodeUnsigned(lengthBuf, buf.readableBytes());
    list.add(lengthBuf);
    list.add(buf.retain());
  }
}
