package io.minimum.minecraft.alien.minecraft.bedrock.util;

import com.google.common.base.Preconditions;
import io.minimum.minecraft.alien.minecraft.shared.codec.Varints;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class McpeUtil {
    private McpeUtil() {

    }

    public static void writeVarintLengthString(ByteBuf buffer, String string) {
        Preconditions.checkNotNull(buffer, "buffer");
        Preconditions.checkNotNull(string, "string");
        int len = ByteBufUtil.utf8Bytes(string);
        Varints.encodeUnsigned(buffer, len);
        buffer.writeCharSequence(string, StandardCharsets.UTF_8);
    }

    public static String readVarintLengthString(ByteBuf buffer) {
        Preconditions.checkNotNull(buffer, "buffer");
        int length = (int) Varints.decodeUnsigned(buffer);
        String data = buffer.toString(buffer.readerIndex(), length, StandardCharsets.UTF_8);
        buffer.skipBytes(length);
        return data;
    }

    public static void writeLELengthString(ByteBuf buffer, String string) {
        Preconditions.checkNotNull(buffer, "buffer");
        Preconditions.checkNotNull(string, "string");
        int len = ByteBufUtil.utf8Bytes(string);
        buffer.writeIntLE(len);
        buffer.writeCharSequence(string, StandardCharsets.UTF_8);
    }

    public static String readLELengthString(ByteBuf buffer) {
        Preconditions.checkNotNull(buffer, "buffer");
        int length = buffer.readIntLE();
        String data = buffer.toString(buffer.readerIndex(), length, StandardCharsets.UTF_8);
        buffer.skipBytes(length);
        return data;
    }

    public static void writeFloatLE(ByteBuf buf, float value) {
        buf.writeIntLE(Float.floatToRawIntBits(value));
    }

    public static float readFloatLE(ByteBuf buf) {
        return Float.intBitsToFloat(buf.readIntLE());
    }

    public static UUID readUuid(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public static void writeUuid(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }
}
