package ru.mifi.oop.finance.infra.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import ru.mifi.oop.finance.core.auth.HashedPassword;
import ru.mifi.oop.finance.core.auth.PasswordHasher;

/**
 * PBKDF2-HMAC-SHA256 password hashing with a random 128-bit salt per user.
 */
public final class Pbkdf2PasswordHasher implements PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public HashedPassword hash(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        secureRandom.nextBytes(salt);
        byte[] hash = derive(rawPassword, salt);
        return new HashedPassword(
                Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(hash));
    }

    @Override
    public boolean matches(String rawPassword, HashedPassword stored) {
        if (rawPassword == null || stored == null) {
            return false;
        }
        try {
            byte[] salt = Base64.getDecoder().decode(stored.salt());
            byte[] expected = Base64.getDecoder().decode(stored.hash());
            byte[] actual = derive(rawPassword, salt);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private byte[] derive(String rawPassword, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Алгоритм хеширования паролей недоступен.", exception);
        } finally {
            spec.clearPassword();
        }
    }
}
