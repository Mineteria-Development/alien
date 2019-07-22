package io.minimum.minecraft.alien.minecraft.bedrock.packet;

import com.google.common.collect.ImmutableList;
import io.minimum.minecraft.alien.minecraft.bedrock.util.McpeUtil;
import io.minimum.minecraft.alien.minecraft.shared.codec.Varints;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

public class McpeChat implements McpePacket {
    public static final int TYPE_RAW = 0;
    public static final int TYPE_CHAT = 1;
    public static final int TYPE_TRANSLATION = 2;
    public static final int TYPE_POPUP = 3;
    public static final int TYPE_JUKEBOX_POPUP = 4;
    public static final int TYPE_TIP = 5;
    public static final int TYPE_SYSTEM = 6;
    public static final int TYPE_WHISPER = 7;
    public static final int TYPE_ANNOUNCEMENT = 8;
    public static final int TYPE_JSON = 9;

    private int type;
    private boolean needsTranslation;
    private String source = "";
    private String message = "";
    private List<String> parameters = ImmutableList.of();

    @Override
    public void decode(ByteBuf buf) {
        this.type = buf.readByte();
        this.needsTranslation = buf.readBoolean();
        switch (type){
            case TYPE_CHAT:
            case TYPE_WHISPER:
            case TYPE_ANNOUNCEMENT:
                this.source = McpeUtil.readVarintLengthString(buf);
            case TYPE_RAW:
            case TYPE_TIP:
            case TYPE_SYSTEM:
            case TYPE_JSON:
                this.message = McpeUtil.readVarintLengthString(buf);
                break;
            case TYPE_TRANSLATION:
            case TYPE_POPUP:
            case TYPE_JUKEBOX_POPUP:
                this.message = McpeUtil.readVarintLengthString(buf);
                int count = (int) Varints.decodeUnsigned(buf);
                this.parameters = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    this.parameters.add(McpeUtil.readVarintLengthString(buf));
                }
                break;
        }

        McpeUtil.readVarintLengthString(buf); // crap
        McpeUtil.readVarintLengthString(buf); // crap
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeByte(type);
        buf.writeBoolean(needsTranslation);
        switch (type) {
            case TYPE_CHAT:
            case TYPE_WHISPER:
            case TYPE_ANNOUNCEMENT:
                McpeUtil.writeVarintLengthString(buf, source);
            case TYPE_RAW:
            case TYPE_TIP:
            case TYPE_SYSTEM:
            case TYPE_JSON:
                McpeUtil.writeVarintLengthString(buf, message);
                break;
            case TYPE_TRANSLATION:
            case TYPE_POPUP:
            case TYPE_JUKEBOX_POPUP:
                McpeUtil.writeVarintLengthString(buf, message);
                Varints.encodeUnsigned(buf, parameters.size());
                for (String parameter : parameters) {
                    McpeUtil.writeVarintLengthString(buf, parameter);
                }
                break;
        }
        McpeUtil.writeVarintLengthString(buf, "");
        McpeUtil.writeVarintLengthString(buf, "");
    }

    @Override
    public boolean handle(McpePacketHandler handler) {
        return handler.handle(this);
    }

    public McpeChat(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public McpeChat() {
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public boolean isNeedsTranslation() {
        return needsTranslation;
    }

    public void setNeedsTranslation(boolean needsTranslation) {
        this.needsTranslation = needsTranslation;
    }

    @Override
    public String toString() {
        return "McpeChat{" +
                "type=" + type +
                ", needsTranslation=" + needsTranslation +
                ", source='" + source + '\'' +
                ", message='" + message + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
