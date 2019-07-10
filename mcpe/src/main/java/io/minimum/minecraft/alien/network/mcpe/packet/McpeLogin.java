package io.minimum.minecraft.alien.network.mcpe.packet;

import io.minimum.minecraft.alien.network.mcpe.McpeUtil;
import io.minimum.minecraft.alien.network.mcpe.util.Varints;
import io.netty.buffer.ByteBuf;
import io.netty.util.AsciiString;

public class McpeLogin implements McpePacket {
    private int protocolVersion;
    private AsciiString jwt;

    @Override
    public void decode(ByteBuf buffer) {
        this.protocolVersion = buffer.readInt();

        Varints.decodeUnsigned(buffer); // ???
        this.jwt = McpeUtil.readLELengthAsciiString(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeInt(protocolVersion);
        //Varints.encodeSigned(this.jwt.length()); // ???
        McpeUtil.writeLELengthAsciiString(buffer, this.jwt);
    }

    @Override
    public void handle(McpePacketHandler handler) {
        handler.handle(this);
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public AsciiString getJwt() {
        return jwt;
    }

    @Override
    public String toString() {
        return "McpeLogin{" +
                "protocolVersion=" + protocolVersion +
                ", jwt=" + jwt +
                '}';
    }
}
