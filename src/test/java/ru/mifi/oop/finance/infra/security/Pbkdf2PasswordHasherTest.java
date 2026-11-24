package ru.mifi.oop.finance.infra.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ru.mifi.oop.finance.core.auth.HashedPassword;

class Pbkdf2PasswordHasherTest {

    private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher();

    @Test
    void verifiesCorrectPasswordAndRejectsWrongOne() {
        HashedPassword stored = hasher.hash("secret123");

        assertTrue(hasher.matches("secret123", stored));
        assertFalse(hasher.matches("secret124", stored));
    }

    @Test
    void usesDifferentSaltForEveryHash() {
        HashedPassword first = hasher.hash("secret123");
        HashedPassword second = hasher.hash("secret123");

        assertNotEquals(first.salt(), second.salt());
        assertNotEquals(first.hash(), second.hash());
    }
}
