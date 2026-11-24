package ru.mifi.oop.finance.core.auth;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import ru.mifi.oop.finance.core.exception.DomainException;
import ru.mifi.oop.finance.core.exception.ValidationException;
import ru.mifi.oop.finance.core.port.UserRepository;
import ru.mifi.oop.finance.core.validation.Validators;

/**
 * Registration and login. Passwords are hashed with a per-user random salt.
 */
public final class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final Clock clock;

    public AuthService(UserRepository userRepository, PasswordHasher passwordHasher, Clock clock) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.clock = clock;
    }

    public UserAccount register(String rawLogin, String rawPassword) {
        String login = Validators.login(rawLogin);
        String password = Validators.password(rawPassword);
        if (userRepository.exists(login)) {
            throw new DomainException("Пользователь «" + login + "» уже зарегистрирован.");
        }
        HashedPassword hashed = passwordHasher.hash(password);
        UserAccount user = new UserAccount(login, hashed, clock.instant());
        userRepository.save(user);
        return user;
    }

    public UserAccount login(String rawLogin, String rawPassword) {
        String login = Validators.login(rawLogin);
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new ValidationException("Пароль не может быть пустым.");
        }
        Optional<UserAccount> found = userRepository.findByLogin(login);
        if (found.isEmpty() || !passwordHasher.matches(rawPassword, found.get().password())) {
            throw new DomainException("Неверный логин или пароль.");
        }
        return found.get();
    }

    public Optional<UserAccount> find(String rawLogin) {
        return userRepository.findByLogin(Validators.login(rawLogin));
    }

    public Instant now() {
        return clock.instant();
    }
}
