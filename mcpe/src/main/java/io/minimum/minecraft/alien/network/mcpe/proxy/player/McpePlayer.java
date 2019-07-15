package io.minimum.minecraft.alien.network.mcpe.proxy.player;

import com.google.common.collect.ImmutableMap;
import io.minimum.minecraft.alien.network.mcpe.data.AuthProfile;
import io.minimum.minecraft.alien.network.mcpe.pipeline.McpeConnection;
import io.minimum.minecraft.alien.network.mcpe.proxy.client.McpeServerConnection;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

public class McpePlayer {
    private final McpeConnection connection;
    private final AuthProfile profile;
    private final Map<String, Object> clientData;
    private @Nullable McpeServerConnection currentServer;

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

    public @Nullable McpeServerConnection getCurrentServer() {
        return currentServer;
    }

    public void setCurrentServer(McpeServerConnection currentServer) {
        this.currentServer = currentServer;
    }
}
