package ru.mifi.oop.finance.core.auth;

import java.util.Objects;

/**
 * Salted password hash. The plain text password is never stored.
 */
public record HashedPassword(String salt, String hash) {

    public HashedPassword {
        Objects.requireNonNull(salt, "salt");
        Objects.requireNonNull(hash, "hash");
    }
}
