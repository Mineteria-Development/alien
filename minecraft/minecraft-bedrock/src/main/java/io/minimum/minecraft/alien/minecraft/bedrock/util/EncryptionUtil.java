package io.minimum.minecraft.alien.minecraft.bedrock.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.minimum.minecraft.alien.minecraft.bedrock.data.AuthProfile;
import io.minimum.minecraft.alien.minecraft.bedrock.packet.McpeLogin;
import io.minimum.minecraft.alien.minecraft.bedrock.packet.McpeServerToClientEncryptionHandshake;
import io.minimum.minecraft.alien.minecraft.bedrock.proxy.player.McpePlayer;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import javax.crypto.KeyAgreement;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.Date;

public class EncryptionUtil {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Gson GSON = new Gson();

    public static final KeyPair SERVER_KEY_PAIR;

    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
            generator.initialize(new ECGenParameterSpec("secp384r1"));
            SERVER_KEY_PAIR = generator.generateKeyPair();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private EncryptionUtil() {

    }

    public static byte[] getServerKey(KeyPair serverPair, PublicKey key, byte[] token) throws InvalidKeyException {
        byte[] sharedSecret = getSharedSecret(serverPair, key);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }

        digest.update(token);
        digest.update(sharedSecret);
        return digest.digest();
    }

    private static byte[] getSharedSecret(KeyPair serverPair, PublicKey clientKey) throws InvalidKeyException {
        KeyAgreement agreement;
        try {
            agreement = KeyAgreement.getInstance("ECDH");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }

        agreement.init(serverPair.getPrivate());
        agreement.doPhase(clientKey, true);
        return agreement.generateSecret();
    }

    public static McpeServerToClientEncryptionHandshake createHandshake(KeyPair serverPair, byte[] token) throws JOSEException, URISyntaxException {
        SignedJWT object = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES384)
                        .x509CertURL(new URI(Base64.getEncoder().encodeToString(serverPair.getPublic().getEncoded())))
                        .build(),
                new JWTClaimsSet.Builder()
                        .claim("salt", Base64.getEncoder().encodeToString(token))
                        .issueTime(new Date())
                        .expirationTime(new Date(System.currentTimeMillis() + 30000))
                        .issuer("Alien")
                        .build()
        );
        object.sign(new ECDSASigner(serverPair.getPrivate(), Curve.P_384));
        return new McpeServerToClientEncryptionHandshake(object.serialize());
    }

    /**
     * This function rewrites the extra data so that the login process can proceed normally.
     * @param o the extra data object
     * @return
     */
    private static JSONObject transformProfile(AuthProfile o) {
        JSONObject object = JSONValue.parse(GSON.toJson(o), new JSONObject());

        // Since we are forging the server profile, move the XUID to a different field
        object.put("__CapturedXUID", object.get("XUID"));
        object.remove("XUID");

        // NukkitX expects the ServerAddress to point to the server address


        return object;
    }

    public static McpeLogin createFakeChain(McpePlayer player) throws JOSEException, URISyntaxException {
        SignedJWT chainData = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES384)
                        .x509CertURL(new URI(Base64.getEncoder().encodeToString(SERVER_KEY_PAIR.getPublic().getEncoded())))
                        .build(),
                new JWTClaimsSet.Builder()
                        .claim("certificateAuthority", true)
                        .claim("extraData", transformProfile(player.getProfile()))
                        .claim("identityPublicKey", Base64.getEncoder().encodeToString(SERVER_KEY_PAIR.getPublic().getEncoded()))
                        .issueTime(new Date())
                        .expirationTime(new Date(System.currentTimeMillis() + 30000))
                        .issuer("Alien")
                        .build()
        );
        chainData.sign(new ECDSASigner(SERVER_KEY_PAIR.getPrivate(), Curve.P_384));
        JWSObject clientData = new JWSObject(
                new JWSHeader.Builder(JWSAlgorithm.ES384)
                        .x509CertURL(new URI(Base64.getEncoder().encodeToString(SERVER_KEY_PAIR.getPublic().getEncoded())))
                        .build(),
                new Payload(new JSONObject(player.getClientData()))
        );
        clientData.sign(new ECDSASigner(SERVER_KEY_PAIR.getPrivate(), Curve.P_384));

        JsonArray fakeChain = new JsonArray();
        fakeChain.add(chainData.serialize());
        JsonObject payload = new JsonObject();
        payload.add("chain", fakeChain);

        return new McpeLogin(player.getConnection().getProtocolVersion(), payload.toString(), clientData.serialize());
    }

    public static byte[] generateRandomToken() {
        byte[] token = new byte[16];
        secureRandom.nextBytes(token);
        return token;
    }
}
