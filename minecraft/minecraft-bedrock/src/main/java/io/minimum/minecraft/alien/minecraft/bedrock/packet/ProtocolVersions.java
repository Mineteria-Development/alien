package io.minimum.minecraft.alien.minecraft.bedrock.packet;

import io.minimum.minecraft.alien.minecraft.bedrock.pipeline.codec.McpePacketRegistry;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ProtocolVersions {
    private ProtocolVersions() {
        throw new AssertionError();
    }

    public static final int PE_1_11 = 354;
    public static final int PE_1_12 = 361;
    private static final Int2ObjectMap<McpePacketRegistry> VERSION_REGISTRIES;

    static {
        Int2ObjectMap<McpePacketRegistry> registries = new Int2ObjectOpenHashMap<>();
        registries.put(PE_1_11, new McpePacketRegistry()
                .register(0x01, McpeLogin.class, McpeLogin::new)
                .register(0x02, McpePlayStatus.class, McpePlayStatus::new)
                .register(0x03, McpeServerToClientEncryptionHandshake.class, McpeServerToClientEncryptionHandshake::new)
                .register(0x04, McpeClientToServerEncryptionHandshake.class, McpeClientToServerEncryptionHandshake::new)
                .register(0x05, McpeDisconnect.class, McpeDisconnect::new)
                .register(0x06, McpeResourcePacks.class, McpeResourcePacks::new)
                .register(0x07, McpeResourcePackStack.class, McpeResourcePackStack::new)
                .register(0x08, McpeResourcePackResponse.class, McpeResourcePackResponse::new)
                .register(0x09, McpeChat.class, McpeChat::new));

        registries.put(PE_1_12, new McpePacketRegistry()
                .register(0x01, McpeLogin.class, McpeLogin::new)
                .register(0x02, McpePlayStatus.class, McpePlayStatus::new)
                .register(0x03, McpeServerToClientEncryptionHandshake.class, McpeServerToClientEncryptionHandshake::new)
                .register(0x04, McpeClientToServerEncryptionHandshake.class, McpeClientToServerEncryptionHandshake::new)
                .register(0x05, McpeDisconnect.class, McpeDisconnect::new)
                .register(0x06, McpeResourcePacks.class, McpeResourcePacks::new)
                .register(0x07, McpeResourcePackStack.class, McpeResourcePackStack::new)
                .register(0x08, McpeResourcePackResponse.class, McpeResourcePackResponse::new)
                .register(0x09, McpeChat.class, McpeChat::new));

        VERSION_REGISTRIES = Int2ObjectMaps.unmodifiable(registries);
    }

    public static @Nullable McpePacketRegistry getRegistry(int protocolVersion) {
        return VERSION_REGISTRIES.get(protocolVersion);
    }
}
