package io.minimum.minecraft.alien.network.mcpe.packet;

import io.minimum.minecraft.alien.network.mcpe.McpeUtil;
import io.minimum.minecraft.alien.network.mcpe.util.Varints;
import io.netty.buffer.ByteBuf;

public class McpeLogin implements McpePacket {
    private int protocolVersion;
    private String jwt;

    public McpeLogin() {
    }

    public McpeLogin(int protocolVersion, String jwt) {
        this.protocolVersion = protocolVersion;
        this.jwt = jwt;
    }

    @Override
    public void decode(ByteBuf buffer) {
        this.protocolVersion = buffer.readInt();
        Varints.decodeUnsigned(buffer); // length of the next string, with length included
        this.jwt = McpeUtil.readLELengthString(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeInt(protocolVersion);
        Varints.encodeSigned(buffer, this.jwt.length() + 4); // length of the next string, with length included
        McpeUtil.writeLELengthString(buffer, this.jwt);
    }

    @Override
    public void handle(McpePacketHandler handler) {
        handler.handle(this);
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getJwt() {
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
