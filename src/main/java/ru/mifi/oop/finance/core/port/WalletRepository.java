package ru.mifi.oop.finance.core.port;

import java.util.Optional;

import ru.mifi.oop.finance.core.model.Wallet;

/**
 * Storage abstraction for wallets. Implementations keep wallets in memory and may
 * transparently persist them to disk.
 */
public interface WalletRepository {

    Optional<Wallet> findByOwner(String login);

    void save(Wallet wallet);
}
