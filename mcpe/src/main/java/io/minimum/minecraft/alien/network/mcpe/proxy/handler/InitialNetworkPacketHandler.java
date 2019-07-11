package io.minimum.minecraft.alien.network.mcpe.proxy.handler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.proc.JWSVerifierFactory;
import io.minimum.minecraft.alien.network.mcpe.data.AuthData;
import io.minimum.minecraft.alien.network.mcpe.listener.McpeConnection;
import io.minimum.minecraft.alien.network.mcpe.packet.*;
import io.minimum.minecraft.alien.network.mcpe.util.EncryptionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;

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

    private final McpeConnection connection;

    public InitialNetworkPacketHandler(McpeConnection connection) {
        this.connection = connection;
    }

    @Override
    public void connected() {

    }

    @Override
    public void disconnected() {

    }

    @Override
    public void handle(McpeLogin packet) {
        // Verify the JWT chain of trust.
        JsonObject desiredData;
        try {
            desiredData = validateAndObtainExtraData(packet);
        } catch (ChainUntrustedException e) {
            LOGGER.error("Unable to verify chain of trust for connection {}", connection.getRemoteAddress(), e);
            connection.close("Internal server error");
            return;
        }

        if (desiredData == null) {
            LOGGER.error("No extraData found for {}", connection.getRemoteAddress());
            connection.close("Internal server error");
            return;
        }

        // We have valid data and can now proceed to enabling encryption.
        AuthData data = GSON.fromJson(desiredData, AuthData.class);
        PublicKey playerKey;
        try {
            playerKey = getKey(data.getIdentityPublicKey());
            startEncryptionHandshake(playerKey);
        } catch (Exception e) {
            LOGGER.error("Can't enable encryption", e);
            connection.close("Internal server error");
        }
    }

    private void startEncryptionHandshake(PublicKey key) throws Exception {
        // Generate a fresh key for each session
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp384r1"));
        KeyPair serverKeyPair = generator.generateKeyPair();

        // Generate required cryptographic keys
        byte[] token = EncryptionUtil.generateRandomToken();
        byte[] serverKey = EncryptionUtil.getServerKey(serverKeyPair, key, token);

        // Send the packet to enable encryption on the client. Once written, immediately enable encryption.
        connection.write(EncryptionUtil.createHandshake(serverKeyPair, token), future -> {
            if (future.isSuccess()) {
                connection.enableEncryption(serverKey);
            } else {
                LOGGER.error("Can't enable encryption", future.cause());
                connection.close("Internal server error");
            }
        });
    }

    @Override
    public void handle(McpeClientToServerEncryptionHandshake packet) {
        connection.close("Encryption test");
    }

    private boolean verify(Key key, JWSObject object) throws JOSEException {
        return object.verify(VERIFIER_FACTORY.createJWSVerifier(object.getHeader(), key));
    }

    private JsonArray obtainChain(McpeLogin packet) {
        JsonObject object = GSON.fromJson(packet.getJwt().toString(), JsonObject.class);
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
