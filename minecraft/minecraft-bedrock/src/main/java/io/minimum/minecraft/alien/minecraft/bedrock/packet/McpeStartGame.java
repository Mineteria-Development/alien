package io.minimum.minecraft.alien.minecraft.bedrock.packet;

import io.minimum.minecraft.alien.minecraft.bedrock.util.McpeUtil;
import io.minimum.minecraft.alien.minecraft.shared.codec.Varints;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// See https://github.com/Hydreon/Steadfast2/blob/master/src/pocketmine/network/protocol/StartGamePacket.php#L58
// Yes, we need to parse the _entire_ packet.
public class McpeStartGame implements McpePacket {
    public static final int GAME_PUBLISH_SETTING_NO_MULTI_PLAY = 0;
    public static final int GAME_PUBLISH_SETTING_INVITE_ONLY = 1;
    public static final int GAME_PUBLISH_SETTING_FRIENDS_ONLY = 2;
    public static final int GAME_PUBLISH_SETTING_FRIENDS_OF_FRIENDS = 3;
    public static final int GAME_PUBLISH_SETTING_PUBLIC = 4;

    private long entityId;
    private long runtimeId;
    private float x;
    private float y;
    private float z;
    private float yaw;
    private float pitch;
    private int seed;
    private int dimension;
    private int generator;
    private int gamemode;
    private int difficulty;
    private int spawnX;
    private int spawnY;
    private int spawnZ;
    private float rainLevel;
    private float lightningLevel;
    private boolean hasAchievementsDisabled;
    private Map<String, Object> gamerules;
    private byte[] palette;
    private String multiplayerCorrelationId;
    private int viewDistance;

    @Override
    public void decode(ByteBuf buf, int protocolVersion) {
        // Entity setup
        this.entityId = Varints.decodeSigned(buf);
        this.runtimeId = Varints.decodeUnsigned(buf);
        this.gamemode = Varints.decodeSigned(buf);
        this.x = buf.readFloatLE();
        this.y = buf.readFloatLE();
        this.z = buf.readFloatLE();
        this.yaw = buf.readFloatLE();
        this.pitch = buf.readFloatLE();

        // Level settings
        this.seed = Varints.decodeSigned(buf);
        this.dimension = Varints.decodeSigned(buf);
        this.generator = Varints.decodeSigned(buf);
        this.gamemode = Varints.decodeSigned(buf);
        this.difficulty = Varints.decodeSigned(buf);

        // In a normal, sane world, this is all we'd need. Unfortunately, Nojang
        // loves to stuff crap in this packet.

        // default spawn 3x VarInt
        this.spawnX = Varints.decodeSigned(buf);
        this.spawnY = (int) Varints.decodeUnsigned(buf);
        this.spawnZ = Varints.decodeSigned(buf);
        this.hasAchievementsDisabled = buf.readBoolean();
        Varints.decodeSigned(buf); // Time

        buf.readBoolean(); // Education mode crap
        buf.readBoolean(); // Education mode crap

        this.rainLevel = buf.readFloatLE();
        this.lightningLevel = buf.readFloatLE();
        buf.skipBytes(1);

        buf.readBoolean(); // is multiplayer game
        buf.readBoolean(); // Broadcast to LAN?
        Varints.decodeSigned(buf); // XBox Live Broadcast setting
        Varints.decodeSigned(buf); // Platform Broadcast setting

        buf.readBoolean(); // commands enabled
        buf.readBoolean(); // isTexturepacksRequired 1x Byte

        this.gamerules = new HashMap<>();

        int gameruleCount = (int) Varints.decodeUnsigned(buf);
        for (int i = 0; i < gameruleCount; i++) {
            String name = McpeUtil.readVarintLengthString(buf);
            int type = (int) Varints.decodeUnsigned(buf);
            if (type == 1) {
                gamerules.put(name, buf.readByte());
            } else if (type == 2) {
                gamerules.put(name, Varints.decodeSigned(buf));
            } else if (type == 3) {
                gamerules.put(name, buf.readFloatLE());
            } else {
                throw new CorruptedFrameException("Unknown gamerule data type " + type);
            }
        }

        buf.readBoolean(); // is bonus chest enabled
        buf.readBoolean(); // is start with map enabled
        Varints.decodeSigned(buf); // permission level
        this.viewDistance = buf.readIntLE();

        // UGH
        buf.skipBytes(6);

        if (protocolVersion >= ProtocolVersions.PE_1_12) {
            buf.readByte();
        }
        // level settings end
        McpeUtil.readVarintLengthString(buf); // level id (random UUID)
        McpeUtil.readVarintLengthString(buf); // level name
        McpeUtil.readVarintLengthString(buf); // template pack id
        buf.readBoolean(); // is trial?
        buf.readLongLE(); // current level time
        Varints.decodeSigned(buf); // enchantment seed

        int pri = buf.readerIndex();
        int paletteItems = (int) Varints.decodeUnsigned(buf);
        for (int i = 0; i < paletteItems; i++) {
            buf.skipBytes((int) Varints.decodeUnsigned(buf));
            buf.skipBytes(protocolVersion >= ProtocolVersions.PE_1_12 ? 4 : 2);
        }

        this.palette = new byte[buf.readerIndex() - pri];
        System.out.println(palette.length);
        buf.getBytes(pri, palette);
        if (protocolVersion >= ProtocolVersions.PE_1_12) {
            Varints.decodeUnsigned(buf);
        }
        multiplayerCorrelationId = McpeUtil.readVarintLengthString(buf);
    }

    @Override
    public void encode(ByteBuf buf, int protocolVersion) {
        // Entity setup
        Varints.encodeUnsigned(buf, entityId);
        Varints.encodeUnsigned(buf, runtimeId);
        Varints.encodeSigned(buf, gamemode);
        buf.writeFloatLE(x);
        buf.writeFloatLE(y);
        buf.writeFloatLE(z);
        buf.writeFloatLE(yaw);
        buf.writeFloatLE(pitch);

        // Level settings
        Varints.encodeSigned(buf, seed);
        Varints.encodeSigned(buf, dimension);
        Varints.encodeSigned(buf, generator);
        Varints.encodeSigned(buf, gamemode);
        Varints.encodeSigned(buf, difficulty);

        // default spawn 3x VarInt
        Varints.encodeSigned(buf, spawnX);
        Varints.encodeUnsigned(buf, spawnY);
        Varints.encodeSigned(buf, spawnZ);
        buf.writeBoolean(hasAchievementsDisabled);

        Varints.encodeSigned(buf, 0); // DayCycleStopTyme 1x VarInt

        buf.writeZero(2); // Education mode crap

        buf.writeFloatLE(rainLevel);
        buf.writeFloatLE(lightningLevel);
        buf.writeZero(1);

        buf.writeBoolean(true); // is multiplayer game
        buf.writeBoolean(false); // Broadcast to LAN?
        Varints.encodeSigned(buf, GAME_PUBLISH_SETTING_PUBLIC); // XBox Live Broadcast setting
        Varints.encodeSigned(buf, GAME_PUBLISH_SETTING_PUBLIC); // Platform Broadcast setting

        buf.writeBoolean(true);	// commands enabled

        buf.writeBoolean(false); // isTexturepacksRequired 1x Byte

        Varints.encodeUnsigned(buf, gamerules.size());
        for (Map.Entry<String, Object> entry : gamerules.entrySet()) {
            McpeUtil.writeVarintLengthString(buf, entry.getKey());
            if (entry.getValue() instanceof Byte) {
                Varints.encodeSigned(buf, 1);
                buf.writeByte((Integer) entry.getValue());
            } else if (entry.getValue() instanceof Integer) {
                Varints.encodeSigned(buf, 2);
                Varints.encodeSigned(buf, (Integer) entry.getValue());
            } else if (entry.getValue() instanceof Float) {
                Varints.encodeSigned(buf, 3);
                buf.writeFloatLE((Float) entry.getValue());
            }
        }

        buf.writeBoolean(false); // is bonus chest enabled
        buf.writeBoolean(false); // is start with map enabled
        Varints.encodeSigned(buf, 1); // permission level
        buf.writeIntLE(viewDistance);

        // UGH
        buf.writeByte(0);
        buf.writeByte(0);
        buf.writeByte(0);
        buf.writeByte(0);
        buf.writeByte(0);
        buf.writeByte(0);

        if (protocolVersion >= ProtocolVersions.PE_1_12) {
            buf.writeByte(0);
        }
        // level settings end
        McpeUtil.writeVarintLengthString(buf, "7a3dad42-991f-4022-b796-413e68a257d3"); // level id (random UUID)
        McpeUtil.writeVarintLengthString(buf, ""); // level name
        McpeUtil.writeVarintLengthString(buf, ""); // template pack id
        buf.writeBoolean(false); // is trial?
        buf.writeLongLE(0); // current level time
        Varints.encodeSigned(buf, 0); // enchantment seed
        buf.writeBytes(palette);
        if (protocolVersion >= ProtocolVersions.PE_1_12) {
            Varints.encodeUnsigned(buf, 0); // item list size
        }
        McpeUtil.writeVarintLengthString(buf, multiplayerCorrelationId);
    }

    @Override
    public boolean handle(McpePacketHandler handler) {
        return handler.handle(this);
    }

    @Override
    public String toString() {
        return "McpeStartGame{" +
                "entityId=" + entityId +
                ", runtimeId=" + runtimeId +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", yaw=" + yaw +
                ", pitch=" + pitch +
                ", seed=" + seed +
                ", dimension=" + dimension +
                ", generator=" + generator +
                ", gamemode=" + gamemode +
                ", difficulty=" + difficulty +
                ", spawnX=" + spawnX +
                ", spawnY=" + spawnY +
                ", spawnZ=" + spawnZ +
                ", rainLevel=" + rainLevel +
                ", lightningLevel=" + lightningLevel +
                ", hasAchievementsDisabled=" + hasAchievementsDisabled +
                ", gamerules=" + gamerules +
                ", palette=" + Arrays.toString(palette) +
                ", multiplayerCorrelationId='" + multiplayerCorrelationId + '\'' +
                ", viewDistance=" + viewDistance +
                '}';
    }
}
