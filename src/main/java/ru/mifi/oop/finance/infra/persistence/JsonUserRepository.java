package ru.mifi.oop.finance.infra.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.mifi.oop.finance.core.auth.HashedPassword;
import ru.mifi.oop.finance.core.auth.UserAccount;
import ru.mifi.oop.finance.core.exception.PersistenceException;
import ru.mifi.oop.finance.core.port.UserRepository;

/**
 * User repository with an in-memory index and a JSON file for durability.
 */
public final class JsonUserRepository implements UserRepository {

    private static final int SCHEMA_VERSION = 1;

    private final Path file;
    private final ObjectMapper mapper;
    private final Map<String, UserAccount> users = new LinkedHashMap<>();

    public JsonUserRepository(Path dataDir, ObjectMapper mapper) {
        this.file = dataDir.resolve("users.json").toAbsolutePath().normalize();
        this.mapper = mapper;
        load();
    }

    @Override
    public synchronized Optional<UserAccount> findByLogin(String login) {
        return Optional.ofNullable(users.get(key(login)));
    }

    @Override
    public synchronized boolean exists(String login) {
        return users.containsKey(key(login));
    }

    @Override
    public synchronized void save(UserAccount user) {
        users.put(key(user.login()), user);
        persist();
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        String content = JsonFiles.read(file);
        if (content.isBlank()) {
            throw new PersistenceException("Файл пользователей " + file + " пуст.");
        }
        UserFile database;
        try {
            database = mapper.readValue(content, UserFile.class);
        } catch (JsonProcessingException exception) {
            throw new PersistenceException("Некорректный файл пользователей " + file + ": "
                    + exception.getOriginalMessage(), exception);
        }
        if (database == null) {
            throw new PersistenceException("Файл пользователей " + file + " не содержит объекта.");
        }
        if (database.schemaVersion() != SCHEMA_VERSION) {
            throw new PersistenceException("Неподдерживаемая версия файла пользователей: "
                    + database.schemaVersion());
        }
        if (database.users() == null) {
            return;
        }
        for (UserRecord record : database.users()) {
            try {
                UserAccount account = new UserAccount(
                        record.login(),
                        new HashedPassword(record.salt(), record.hash()),
                        record.createdAt());
                String key = key(account.login());
                if (users.put(key, account) != null) {
                    throw new PersistenceException("Файл пользователей содержит дубликат логина «"
                            + account.login() + "».");
                }
            } catch (PersistenceException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new PersistenceException("Некорректная запись пользователя в файле " + file + ": "
                        + exception.getMessage(), exception);
            }
        }
    }

    private void persist() {
        List<UserRecord> records = new ArrayList<>();
        for (UserAccount user : users.values()) {
            records.add(new UserRecord(user.login(), user.password().salt(), user.password().hash(),
                    user.createdAt()));
        }
        UserFile database = new UserFile(SCHEMA_VERSION, records);
        try {
            JsonFiles.write(file, mapper.writeValueAsString(database));
        } catch (JsonProcessingException exception) {
            throw new PersistenceException("Не удалось сериализовать пользователей: "
                    + exception.getMessage(), exception);
        }
    }

    private static String key(String login) {
        return login == null ? "" : login.strip().toLowerCase(Locale.ROOT);
    }

    /**
     * On-disk representation of the users database.
     */
    public record UserFile(int schemaVersion, List<UserRecord> users) {
    }

    /**
     * On-disk representation of one user.
     */
    public record UserRecord(String login, String salt, String hash, Instant createdAt) {
    }
}
