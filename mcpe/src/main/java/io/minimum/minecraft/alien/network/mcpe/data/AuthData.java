package io.minimum.minecraft.alien.network.mcpe.data;

public class AuthData {
    private final String identityPublicKey;
    private final AuthProfile extraData;

    public AuthData(String identityPublicKey, AuthProfile extraData) {
        this.identityPublicKey = identityPublicKey;
        this.extraData = extraData;
    }

    public String getIdentityPublicKey() {
        return identityPublicKey;
    }

    public AuthProfile getExtraData() {
        return extraData;
    }

    @Override
    public String toString() {
        return "AuthData{" +
                "identityPublicKey='" + identityPublicKey + '\'' +
                ", extraData=" + extraData +
                '}';
    }
}
