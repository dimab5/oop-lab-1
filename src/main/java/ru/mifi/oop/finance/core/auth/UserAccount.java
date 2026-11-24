package ru.mifi.oop.finance.core.auth;

import java.time.Instant;
import java.util.Objects;

import ru.mifi.oop.finance.core.validation.Validators;

/**
 * Registered user. Login is stored in normalized lower case.
 */
public record UserAccount(String login, HashedPassword password, Instant createdAt) {

    public UserAccount {
        login = Validators.login(login);
        Objects.requireNonNull(password, "password");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public String displayLogin() {
        return login;
    }
}
