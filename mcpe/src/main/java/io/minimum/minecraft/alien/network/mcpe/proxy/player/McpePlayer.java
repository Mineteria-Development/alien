package io.minimum.minecraft.alien.network.mcpe.proxy.player;

import com.google.common.collect.ImmutableMap;
import io.minimum.minecraft.alien.network.mcpe.data.AuthProfile;
import io.minimum.minecraft.alien.network.mcpe.pipeline.McpeConnection;

import java.util.Map;

public class McpePlayer {
    private final McpeConnection connection;
    private final AuthProfile profile;
    private final Map<String, Object> clientData;

    public McpePlayer(McpeConnection connection, AuthProfile profile, Map<String, Object> clientData) {
        this.connection = connection;
        this.profile = profile;
        this.clientData = ImmutableMap.copyOf(clientData);
    }

    public McpeConnection getConnection() {
        return connection;
    }

    public AuthProfile getProfile() {
        return profile;
    }

    public Map<String, Object> getClientData() {
        return clientData;
    }
}
