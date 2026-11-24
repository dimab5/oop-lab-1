package ru.mifi.oop.finance.core.port;

import java.util.Optional;

import ru.mifi.oop.finance.core.auth.UserAccount;

/**
 * Storage abstraction for registered users.
 */
public interface UserRepository {

    Optional<UserAccount> findByLogin(String login);

    boolean exists(String login);

    void save(UserAccount user);
}
