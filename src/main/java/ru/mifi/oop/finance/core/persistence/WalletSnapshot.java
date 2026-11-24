package ru.mifi.oop.finance.core.persistence;

import java.time.Instant;
import java.util.List;

/**
 * JSON-friendly representation of a wallet. Kept separate from the domain aggregate so
 * storage format can evolve independently.
 */
public record WalletSnapshot(
        int schemaVersion,
        String owner,
        Instant createdAt,
        Instant savedAt,
        List<CategorySnapshot> categories,
        List<TransactionSnapshot> transactions,
        List<BudgetSnapshot> budgets) {
}
