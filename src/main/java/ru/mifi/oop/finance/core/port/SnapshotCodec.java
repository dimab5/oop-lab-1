package ru.mifi.oop.finance.core.port;

import java.nio.file.Path;

import ru.mifi.oop.finance.core.persistence.WalletSnapshot;

/**
 * Reads and writes JSON wallet snapshots. Used both for automatic persistence and
 * for explicit export/import.
 */
public interface SnapshotCodec {

    WalletSnapshot read(Path path);

    void write(Path path, WalletSnapshot snapshot);
}
