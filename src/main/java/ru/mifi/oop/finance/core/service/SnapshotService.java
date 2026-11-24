package ru.mifi.oop.finance.core.service;

import java.nio.file.Path;
import java.time.Clock;

import ru.mifi.oop.finance.core.exception.DomainException;
import ru.mifi.oop.finance.core.model.Wallet;
import ru.mifi.oop.finance.core.persistence.WalletSnapshot;
import ru.mifi.oop.finance.core.persistence.WalletSnapshotMapper;
import ru.mifi.oop.finance.core.port.SnapshotCodec;
import ru.mifi.oop.finance.core.port.WalletRepository;

/**
 * Explicit JSON export/import of a wallet snapshot. Import validates the whole file
 * before replacing the current wallet.
 */
public final class SnapshotService {

    private final WalletRepository walletRepository;
    private final SnapshotCodec codec;
    private final WalletSnapshotMapper mapper;
    private final Clock clock;

    public SnapshotService(
            WalletRepository walletRepository,
            SnapshotCodec codec,
            WalletSnapshotMapper mapper,
            Clock clock) {
        this.walletRepository = walletRepository;
        this.codec = codec;
        this.mapper = mapper;
        this.clock = clock;
    }

    public ExportResult export(Wallet wallet, Path path) {
        WalletSnapshot snapshot = mapper.toSnapshot(wallet, clock.instant());
        codec.write(path, snapshot);
        return new ExportResult(
                path.toAbsolutePath().normalize(),
                snapshot.categories().size(),
                snapshot.transactions().size(),
                snapshot.budgets().size());
    }

    public Wallet importSnapshot(Wallet currentWallet, Path path, boolean force) {
        WalletSnapshot snapshot = codec.read(path);
        Wallet imported = mapper.fromSnapshot(snapshot, currentWallet.ownerLogin());
        if (!force && !currentWallet.isEmpty()) {
            throw new DomainException("Текущий кошелёк не пуст. Импорт заменит все данные. "
                    + "Повторите команду с флагом --force, если это действительно нужно.");
        }
        walletRepository.save(imported);
        return imported;
    }
}
