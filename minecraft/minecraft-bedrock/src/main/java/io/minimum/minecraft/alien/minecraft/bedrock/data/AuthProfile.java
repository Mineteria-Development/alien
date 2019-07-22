package io.minimum.minecraft.alien.minecraft.bedrock.data;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class AuthProfile {
    @SerializedName("XUID")
    private final String xuid;
    private final UUID identity;
    private final String displayName;

    public AuthProfile(String xuid, UUID identity, String displayName) {
        this.xuid = xuid;
        this.identity = identity;
        this.displayName = displayName;
    }

    public String getXuid() {
        return xuid;
    }

    public UUID getIdentity() {
        return identity;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return "AuthenticationData{" +
                "xuid='" + xuid + '\'' +
                ", identity=" + identity +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
