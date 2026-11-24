package ru.mifi.oop.finance.core.persistence;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Serialized wallet operation.
 */
public record TransactionSnapshot(
        String id,
        String type,
        String category,
        BigDecimal amount,
        String note,
        Instant occurredAt) {
}
