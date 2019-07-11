package io.minimum.minecraft.alien.natives.encryption;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

public interface VelocityCipherFactory {

  VelocityCipher forEncryption(SecretKey key, byte[] iv) throws GeneralSecurityException;

  VelocityCipher forDecryption(SecretKey key, byte[] iv) throws GeneralSecurityException;
}
