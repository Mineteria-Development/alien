package io.minimum.minecraft.alien.minecraft.bedrock.packet;

import io.minimum.minecraft.alien.minecraft.bedrock.util.McpeUtil;
import io.netty.buffer.ByteBuf;

public class McpeServerToClientEncryptionHandshake implements McpePacket {
    private String jwt;

    public McpeServerToClientEncryptionHandshake() {
    }

    public McpeServerToClientEncryptionHandshake(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public void decode(ByteBuf buf) {
        this.jwt = McpeUtil.readVarintLengthString(buf);
    }

    @Override
    public void encode(ByteBuf buf) {
        McpeUtil.writeVarintLengthString(buf, this.jwt);
    }

    @Override
    public boolean handle(McpePacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public String toString() {
        return "McpeServerToClientEncryptionHandshake{" +
                "jwt='" + jwt + '\'' +
                '}';
    }
}
