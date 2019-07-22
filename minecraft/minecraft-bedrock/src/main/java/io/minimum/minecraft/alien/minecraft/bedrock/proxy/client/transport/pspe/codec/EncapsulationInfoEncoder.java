package io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.transport.pspe.codec;

import io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.transport.pspe.EncapsulationInfo;
import io.minimum.minecraft.alien.minecraft.shared.codec.Varints;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class EncapsulationInfoEncoder extends MessageToByteEncoder<EncapsulationInfo> {
    public static final EncapsulationInfoEncoder INSTANCE = new EncapsulationInfoEncoder();

    private EncapsulationInfoEncoder() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, EncapsulationInfo msg, ByteBuf out) throws Exception {
        System.out.println("Encoding " + msg);
        out.writeByte(0);
        Varints.encodeUnsigned(out, 1); // protocol version
        if (msg.getAddress() != null) {
            out.writeBoolean(true);
            byte[] addr = msg.getAddress().getAddress().getAddress();
            Varints.encodeUnsigned(out, addr.length);
            out.writeBytes(addr);
            Varints.encodeUnsigned(out, msg.getAddress().getPort());
        } else {
            out.writeBoolean(false);
        }
        out.writeBoolean(msg.isCompressed());
    }
}
