package io.minimum.minecraft.alien.minecraft.bedrock.packet;

import io.minimum.minecraft.alien.minecraft.bedrock.util.McpeUtil;
import io.minimum.minecraft.alien.minecraft.shared.codec.Varints;
import io.netty.buffer.ByteBuf;

public class McpeLogin implements McpePacket {
    private int protocolVersion;
    private String chainData;
    private String clientData;

    public McpeLogin() {
    }

    public McpeLogin(int protocolVersion, String chainData) {
        this.protocolVersion = protocolVersion;
        this.chainData = chainData;
        this.chainData = "";
    }

    public McpeLogin(int protocolVersion, String chainData, String clientData) {
        this.protocolVersion = protocolVersion;
        this.chainData = chainData;
        this.clientData = clientData;
    }

    @Override
    public void decode(ByteBuf buffer) {
        this.protocolVersion = buffer.readInt();
        Varints.decodeUnsigned(buffer); // length of the next two strings, with length included
        this.chainData = McpeUtil.readLELengthString(buffer);
        this.clientData = McpeUtil.readLELengthString(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeInt(protocolVersion);
        Varints.encodeUnsigned(buffer, this.chainData.length() + this.clientData.length() + 8); // length of the next string, with length included
        McpeUtil.writeLELengthString(buffer, this.chainData);
        McpeUtil.writeLELengthString(buffer, this.clientData);
    }

    @Override
    public boolean handle(McpePacketHandler handler) {
        return handler.handle(this);
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getChainData() {
        return chainData;
    }

    public String getClientData() {
        return clientData;
    }

    @Override
    public String toString() {
        return "McpeLogin{" +
                "protocolVersion=" + protocolVersion +
                ", chainData='" + chainData + '\'' +
                ", clientData='" + clientData + '\'' +
                '}';
    }
}
