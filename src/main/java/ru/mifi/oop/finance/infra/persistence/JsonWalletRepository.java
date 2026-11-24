package ru.mifi.oop.finance.infra.persistence;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import ru.mifi.oop.finance.core.exception.PersistenceException;
import ru.mifi.oop.finance.core.model.Wallet;
import ru.mifi.oop.finance.core.persistence.WalletSnapshot;
import ru.mifi.oop.finance.core.persistence.WalletSnapshotMapper;
import ru.mifi.oop.finance.core.port.SnapshotCodec;
import ru.mifi.oop.finance.core.port.WalletRepository;
import ru.mifi.oop.finance.core.validation.Validators;

/**
 * Wallet repository that caches loaded wallets in memory and stores each wallet in a
 * separate JSON file. A wallet is loaded from disk on first access after login.
 */
public final class JsonWalletRepository implements WalletRepository {

    private final Path walletsDirectory;
    private final SnapshotCodec codec;
    private final WalletSnapshotMapper mapper;
    private final Clock clock;
    private final Map<String, Wallet> cache = new LinkedHashMap<>();

    public JsonWalletRepository(Path dataDir, SnapshotCodec codec, WalletSnapshotMapper mapper, Clock clock) {
        this.walletsDirectory = dataDir.resolve("wallets").toAbsolutePath().normalize();
        this.codec = codec;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    public synchronized Optional<Wallet> findByOwner(String login) {
        String normalized = Validators.login(login);
        Wallet cached = cache.get(normalized);
        if (cached != null) {
            return Optional.of(cached);
        }
        Path path = pathFor(normalized);
        if (!Files.isRegularFile(path)) {
            return Optional.empty();
        }
        WalletSnapshot snapshot = codec.read(path);
        Wallet wallet = mapper.fromSnapshot(snapshot, normalized);
        cache.put(normalized, wallet);
        return Optional.of(wallet);
    }

    @Override
    public synchronized void save(Wallet wallet) {
        String login = Validators.login(wallet.ownerLogin());
        if (!login.equals(wallet.ownerLogin())) {
            throw new PersistenceException("Владелец кошелька не совпадает с нормализованным логином.");
        }
        WalletSnapshot snapshot = mapper.toSnapshot(wallet, clock.instant());
        codec.write(pathFor(login), snapshot);
        cache.put(login, wallet);
    }

    public Path pathFor(String login) {
        String normalized = Validators.login(login);
        String fileName = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(normalized.getBytes(StandardCharsets.UTF_8)) + ".json";
        return walletsDirectory.resolve(fileName);
    }
}
