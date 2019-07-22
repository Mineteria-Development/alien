package io.minimum.minecraft.alien.minecraft.bedrock.packet;

import io.minimum.minecraft.alien.minecraft.bedrock.util.McpeUtil;
import io.netty.buffer.ByteBuf;

public class McpeDisconnect implements McpePacket {
    private boolean hideDisconnectScreen;
    private String message = "";

    public McpeDisconnect() {
    }

    public McpeDisconnect(String message) {
        this.hideDisconnectScreen = false;
        this.message = message;
    }

    @Override
    public void decode(ByteBuf buf) {
        this.hideDisconnectScreen = buf.readBoolean();
        if (!this.hideDisconnectScreen) {
            this.message = McpeUtil.readVarintLengthString(buf);
        }
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeBoolean(hideDisconnectScreen);
        if (!this.hideDisconnectScreen) {
            McpeUtil.writeVarintLengthString(buf, message);
        }
    }

    @Override
    public boolean handle(McpePacketHandler handler) {
        return handler.handle(this);
    }

    public boolean isHideDisconnectScreen() {
        return hideDisconnectScreen;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "McpeDisconnect{" +
                "hideDisconnectScreen=" + hideDisconnectScreen +
                ", message='" + message + '\'' +
                '}';
    }
}
