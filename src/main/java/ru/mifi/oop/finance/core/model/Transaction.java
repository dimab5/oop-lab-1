package ru.mifi.oop.finance.core.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * An immutable income or expense operation.
 */
public record Transaction(
        UUID id,
        TransactionType type,
        String category,
        BigDecimal amount,
        String note,
        Instant occurredAt) {

    public Transaction {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(occurredAt, "occurredAt");
        category = Objects.requireNonNull(category, "category").strip();
        note = note == null ? "" : note.strip();
        amount = Money.positive(amount, "Сумма операции");
    }

    public static Transaction create(
            TransactionType type,
            String category,
            BigDecimal amount,
            String note,
            Instant occurredAt) {
        return new Transaction(UUID.randomUUID(), type, category, amount, note, occurredAt);
    }
}
