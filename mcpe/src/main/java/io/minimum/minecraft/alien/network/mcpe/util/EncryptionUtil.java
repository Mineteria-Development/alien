package io.minimum.minecraft.alien.network.mcpe.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.minimum.minecraft.alien.network.mcpe.packet.McpeServerToClientEncryptionHandshake;

import javax.crypto.KeyAgreement;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.util.Base64;
import java.util.Date;

public class EncryptionUtil {
    private static final SecureRandom secureRandom = new SecureRandom();

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

    public static byte[] generateRandomToken() {
        byte[] token = new byte[16];
        secureRandom.nextBytes(token);
        return token;
    }
}
