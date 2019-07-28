package io.minimum.minecraft.alien.minecraft.bedrock.proxy.player;

import com.google.common.collect.ImmutableMap;
import io.minimum.minecraft.alien.minecraft.bedrock.data.AuthProfile;
import io.minimum.minecraft.alien.minecraft.bedrock.pipeline.BedrockConnection;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.rewrite.NoopPacketRewriter;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.rewrite.PacketRewriter;
import io.minimum.minecraft.alien.shared.network.MinecraftConnectionAssociation;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.client.McpeServerConnection;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

public class McpePlayer implements MinecraftConnectionAssociation {
    private final BedrockConnection connection;
    private final AuthProfile profile;
    private final Map<String, Object> clientData;
    private @Nullable McpeServerConnection currentServer;
    private PacketRewriter rewriter;

    public McpePlayer(BedrockConnection connection, AuthProfile profile, Map<String, Object> clientData) {
        this.connection = connection;
        this.profile = profile;
        this.clientData = ImmutableMap.copyOf(clientData);
        this.rewriter = new NoopPacketRewriter();
    }

    public BedrockConnection getConnection() {
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

    @Override
    public String toString() {
        return "Player [" + profile.getDisplayName() + "] (" + connection.getRemoteAddress() + ")";
    }

    public PacketRewriter getRewriter() {
        return rewriter;
    }

    public void onServerSwitch(int newEntityId) {
        rewriter.setServerEntityId(newEntityId);
    }
}
