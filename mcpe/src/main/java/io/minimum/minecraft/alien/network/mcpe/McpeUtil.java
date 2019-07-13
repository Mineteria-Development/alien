package io.minimum.minecraft.alien.network.mcpe;

import com.google.common.base.Preconditions;
import io.minimum.minecraft.alien.network.mcpe.util.Attribute;
import io.minimum.minecraft.alien.network.mcpe.util.Skin;
import io.minimum.minecraft.alien.network.mcpe.util.Varints;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.util.AsciiString;
//import org.spongepowered.math.vector.Vector3f;
//import org.spongepowered.math.vector.Vector3i;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    /*

    public static void writeBlockCoords(ByteBuf buf, Vector3i vector3i) {
        Varints.encodeSigned(buf, vector3i.getX());
        Varints.encodeUnsigned(buf, vector3i.getY());
        Varints.encodeSigned(buf, vector3i.getZ());
    }

    public static Vector3i readBlockCoords(ByteBuf buf) {
        int x = Varints.decodeSigned(buf);
        int y = (int) Varints.decodeUnsigned(buf);
        int z = Varints.decodeSigned(buf);
        return new Vector3i(x, y, z);
    }

    public static void writeVector3f(ByteBuf buf, Vector3f vector3f) {
        writeFloatLE(buf, vector3f.getX());
        writeFloatLE(buf, vector3f.getY());
        writeFloatLE(buf, vector3f.getZ());
    }

    public static Vector3f readVector3f(ByteBuf buf) {
        double x = readFloatLE(buf);
        double y = readFloatLE(buf);
        double z = readFloatLE(buf);
        return new Vector3f(x, y, z);
    }
*/
    public static Collection<Attribute> readAttributes(ByteBuf buf) {
        List<Attribute> attributes = new ArrayList<>();
        int size = (int) Varints.decodeUnsigned(buf);

        for (int i = 0; i < size; i++) {
            float min = readFloatLE(buf);
            float max = readFloatLE(buf);
            float val = readFloatLE(buf);
            float defaultVal = readFloatLE(buf);
            String name = readVarintLengthString(buf);

            attributes.add(new Attribute(name, min, max, val, defaultVal));
        }

        return attributes;
    }

    public static void writeFloatLE(ByteBuf buf, float value) {
        buf.writeIntLE(Float.floatToRawIntBits(value));
    }

    public static float readFloatLE(ByteBuf buf) {
        return Float.intBitsToFloat(buf.readIntLE());
    }

    public static void writeAttributes(ByteBuf buf, Collection<Attribute> attributeList) {
        Varints.encodeUnsigned(buf, attributeList.size());
        for (Attribute attribute : attributeList) {
            writeFloatLE(buf, attribute.getMinimumValue());
            writeFloatLE(buf, attribute.getMaximumValue());
            writeFloatLE(buf, attribute.getValue());
            writeFloatLE(buf, attribute.getDefaultValue());
            writeVarintLengthString(buf, attribute.getName());
        }
    }

    public static void writeSkin(ByteBuf buf, Skin skin) {
        byte[] texture = skin.getTexture();
        writeVarintLengthString(buf, skin.getType());
        Varints.encodeUnsigned(buf, texture.length);
        buf.writeBytes(texture);
    }

    public static UUID readUuid(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    public static void writeUuid(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }
}
