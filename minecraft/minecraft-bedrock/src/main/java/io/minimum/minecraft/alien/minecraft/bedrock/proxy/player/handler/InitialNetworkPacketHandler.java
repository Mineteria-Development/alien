package io.minimum.minecraft.alien.minecraft.bedrock.proxy.player.handler;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.proc.JWSVerifierFactory;
import io.minimum.minecraft.alien.minecraft.bedrock.data.AuthData;
import io.minimum.minecraft.alien.minecraft.bedrock.packet.McpeClientToServerEncryptionHandshake;
import io.minimum.minecraft.alien.minecraft.bedrock.packet.McpeLogin;
import io.minimum.minecraft.alien.minecraft.bedrock.packet.McpePacketHandler;
import io.minimum.minecraft.alien.minecraft.bedrock.pipeline.BedrockConnection;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.player.McpePlayer;
import io.minimum.minecraft.alien.minecraft.bedrock.util.EncryptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;

public class InitialNetworkPacketHandler implements McpePacketHandler {
    private static final Gson GSON = new Gson();
    private static final String MOJANG_PUBLIC_KEY_BASE64 =
            "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE8ELkixyLcwlZryUQcu1TvPOmI2B7vX83ndnWRUaXm74wFfa5f/lwQNTfrLVHa2PmenpGI6JhIMUJaWZrjmMj90NoKNFSNBuKdm8rYiXsfaz3K36x/1U26HpG0ZxK/V1V";
    private static final PublicKey MOJANG_PUBLIC_KEY;
    private static final Logger LOGGER = LogManager.getLogger(InitialNetworkPacketHandler.class);
    private static final JWSVerifierFactory VERIFIER_FACTORY = new DefaultJWSVerifierFactory();

    static {
        try {
            MOJANG_PUBLIC_KEY = KeyFactory.getInstance("EC").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(MOJANG_PUBLIC_KEY_BASE64)));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static PublicKey getKey(String b64) throws InvalidKeySpecException {
        try {
            return KeyFactory.getInstance("EC").generatePublic(
                    new X509EncodedKeySpec(Base64.getDecoder().decode(b64)));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private final BedrockConnection connection;
    private AuthData authData;
    private Map<String, Object> clientData;

    public InitialNetworkPacketHandler(BedrockConnection connection) {
        this.connection = connection;
    }

    @Override
    public void exception(Throwable throwable) {

    }

    @Override
    public boolean handle(McpeLogin packet) {
        // Verify the JWT chain of trust.
        JsonObject desiredData;
        try {
            desiredData = validateAndObtainExtraData(packet);
        } catch (ChainUntrustedException e) {
            LOGGER.error("Unable to verify chain of trust for connection {}", connection.getRemoteAddress(), e);
            connection.closeWith("Internal server error");
            return true;
        }

        if (desiredData == null) {
            LOGGER.error("No extraData found for {}", connection.getRemoteAddress());
            connection.closeWith("Internal server error");
            return true;
        }

        // We have valid data and can now proceed to enabling encryption.
        try {
            this.authData = GSON.fromJson(desiredData, AuthData.class);
            this.clientData = JWSObject.parse(packet.getClientData()).getPayload().toJSONObject();
            PublicKey playerKey = getKey(authData.getIdentityPublicKey());
            startEncryptionHandshake(playerKey);
        } catch (Exception e) {
            LOGGER.error("Can't enable encryption", e);
            connection.closeWith("Internal server error");
        }
        return true;
    }

    private void startEncryptionHandshake(PublicKey key) throws Exception {
        // Generate required cryptographic keys
        byte[] token = EncryptionUtil.generateRandomToken();
        byte[] serverKey = EncryptionUtil.getServerKey(EncryptionUtil.SERVER_KEY_PAIR, key, token);

        // Send the packet to enable encryption on the client. Once written, immediately enable encryption.
        connection.write(EncryptionUtil.createHandshake(EncryptionUtil.SERVER_KEY_PAIR, token), future -> {
            if (future.isSuccess()) {
                connection.enableEncryption(serverKey);
            } else {
                LOGGER.error("Can't enable encryption", future.cause());
                connection.closeWith("Internal server error");
            }
        });
    }

    @Override
    public boolean handle(McpeClientToServerEncryptionHandshake packet) {
        handleResourcePack();
        return true;
    }

    private void handleResourcePack() {
        Preconditions.checkState(this.authData != null, "No auth yet");
        McpePlayer player = new McpePlayer(connection, this.authData.getExtraData(), clientData);
        ResourcePackPacketHandler newHandler = new ResourcePackPacketHandler(player);
        connection.setAssociation(player);
        connection.setSessionHandler(newHandler);
    }

    private boolean verify(Key key, JWSObject object) throws JOSEException {
        return object.verify(VERIFIER_FACTORY.createJWSVerifier(object.getHeader(), key));
    }

    private JsonArray obtainChain(McpeLogin packet) {
        JsonObject object = GSON.fromJson(packet.getChainData().toString(), JsonObject.class);
        return object.getAsJsonArray("chain");
    }

    private JsonObject validateAndObtainExtraData(McpeLogin login) throws ChainUntrustedException {
        JsonArray chain = obtainChain(login);
        try {
            PublicKey lastKey = null;
            boolean trustedChain = false;
            for (JsonElement element : chain) {
                JWSObject token = JWSObject.parse(element.getAsString());
                if (!trustedChain) {
                    trustedChain = verify(MOJANG_PUBLIC_KEY, token);
                }
                if (lastKey != null) {
                    if (!verify(lastKey, token)) {
                        throw new ChainUntrustedException();
                    }
                }

                LOGGER.info("Header: {}, Payload: \"{}\"", token.getHeader().toJSONObject().toJSONString(), token.getPayload().toString());

                JsonObject object = GSON.fromJson(token.getPayload().toString(), JsonObject.class);
                lastKey = getKey(object.getAsJsonPrimitive("identityPublicKey").getAsString());

                if (object.has("extraData")) {
                    return object;
                }
            }
        } catch (ParseException | JOSEException | InvalidKeySpecException e) {
            throw new ChainUntrustedException(e);
        }

        throw new ChainUntrustedException("The chain validated, but no extra data was found");
    }

    private static class ChainUntrustedException extends Exception {
        public ChainUntrustedException() {
            super("The provided JWT data was not signed by Mojang");
        }

        public ChainUntrustedException(String message) {
            super(message);
        }

        public ChainUntrustedException(Throwable cause) {
            super(cause);
        }
    }
}
