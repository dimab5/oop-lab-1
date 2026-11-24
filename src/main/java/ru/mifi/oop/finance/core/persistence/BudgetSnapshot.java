package ru.mifi.oop.finance.core.persistence;

import java.math.BigDecimal;

/**
 * Serialized budget limit.
 */
public record BudgetSnapshot(String category, BigDecimal limit) {
}
