package ru.mifi.oop.finance.core.auth;

/**
 * Abstraction around password hashing so tests and future storage can replace it.
 */
public interface PasswordHasher {

    HashedPassword hash(String rawPassword);

    boolean matches(String rawPassword, HashedPassword stored);
}
