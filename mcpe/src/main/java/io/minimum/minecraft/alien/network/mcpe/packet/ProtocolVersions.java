package io.minimum.minecraft.alien.network.mcpe.packet;

import io.minimum.minecraft.alien.network.mcpe.pipeline.codec.McpePacketRegistry;

public class ProtocolVersions {
    private ProtocolVersions() {
        throw new AssertionError();
    }

    public static final McpePacketRegistry PE_1_11 = new McpePacketRegistry()
            .register(0x01, McpeLogin.class, McpeLogin::new)
            .register(0x02, McpePlayStatus.class, McpePlayStatus::new)
            .register(0x03, McpeServerToClientEncryptionHandshake.class, McpeServerToClientEncryptionHandshake::new)
            .register(0x04, McpeClientToServerEncryptionHandshake.class, McpeClientToServerEncryptionHandshake::new)
            .register(0x05, McpeDisconnect.class, McpeDisconnect::new)
            .register(0x06, McpeResourcePacks.class, McpeResourcePacks::new)
            .register(0x07, McpeResourcePackStack.class, McpeResourcePackStack::new)
            .register(0x08, McpeResourcePackResponse.class, McpeResourcePackResponse::new);
}
