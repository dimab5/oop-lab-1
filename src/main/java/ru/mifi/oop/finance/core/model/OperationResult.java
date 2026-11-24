package ru.mifi.oop.finance.core.model;

import java.math.BigDecimal;

/**
 * Details of a successfully added income or expense.
 */
public record OperationResult(Transaction transaction, boolean categoryCreated, BigDecimal balance) {
}
