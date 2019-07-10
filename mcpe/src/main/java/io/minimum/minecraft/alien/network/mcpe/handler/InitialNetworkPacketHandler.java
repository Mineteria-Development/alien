package io.minimum.minecraft.alien.network.mcpe.handler;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
import com.nimbusds.jose.proc.JWSVerifierFactory;
import io.minimum.minecraft.alien.network.mcpe.listener.McpeConnection;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeLogin;
import io.minimum.minecraft.alien.network.mcpe.packet.McpePacketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
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

    private static PublicKey getKey(String b64) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance("EC").generatePublic(
                new X509EncodedKeySpec(Base64.getDecoder().decode(b64)));
    }

    private final McpeConnection connection;

    public InitialNetworkPacketHandler(McpeConnection connection) {
        this.connection = connection;
    }

    @Override
    public void disconnected() {

    }

    @Override
    public void handle(McpeLogin packet) {
        // This is the first MCPE packet we receive once RakNet negotiation succeeds. It contains the protocol version
        // and a chain of trust of JSON Web Tokens.

        // Verify the JWT chain of trust.
        JsonArray chain = obtainChain(packet);
        JsonObject desiredData = null;

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
                        throw new JOSEException("Unable to verify key in chain.");
                    }
                }

                JsonObject object = GSON.fromJson(token.getPayload().toString(), JsonObject.class);
                lastKey = getKey(object.getAsJsonPrimitive("identityPublicKey").getAsString());

                if (object.has("extraData")) {
                    desiredData = object;
                    break;
                }
            }
        } catch (JOSEException | InvalidKeySpecException | ParseException | NoSuchAlgorithmException e) {
            LOGGER.error("Unable to verify chain of trust for connection {}", connection.getRemoteAddress(), e);
            connection.close();
        }

        if (desiredData == null) {
            LOGGER.error("No extraData found for {}", connection.getRemoteAddress());
            connection.close();
        }

        LOGGER.info("I have a gift for you. {}", desiredData);
    }

    @Override
    public void connected() {

    }

    private boolean verify(Key key, JWSObject object) throws JOSEException {
        return object.verify(VERIFIER_FACTORY.createJWSVerifier(object.getHeader(), key));
    }

    private JsonArray obtainChain(McpeLogin packet) {
        JsonObject object = GSON.fromJson(packet.getJwt().toString(), JsonObject.class);
        return object.getAsJsonArray("chain");
    }
}
