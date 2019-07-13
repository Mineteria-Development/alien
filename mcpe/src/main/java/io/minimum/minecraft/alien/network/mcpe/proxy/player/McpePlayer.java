package io.minimum.minecraft.alien.network.mcpe.proxy.player;

import io.minimum.minecraft.alien.network.mcpe.data.AuthProfile;
import io.minimum.minecraft.alien.network.mcpe.listener.McpeConnection;

public class McpePlayer {
    private final McpeConnection connection;
    private final AuthProfile profile;

    public McpePlayer(McpeConnection connection, AuthProfile profile) {
        this.connection = connection;
        this.profile = profile;
    }

    public McpeConnection getConnection() {
        return connection;
    }

    public AuthProfile getProfile() {
        return profile;
    }
}
