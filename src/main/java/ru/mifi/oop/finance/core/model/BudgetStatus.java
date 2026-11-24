package ru.mifi.oop.finance.core.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Snapshot of a budget with current spending and the remaining limit.
 */
public record BudgetStatus(
        String category,
        BigDecimal limit,
        BigDecimal spent,
        BigDecimal remaining,
        BigDecimal usedPercent) {

    public static final BigDecimal WARNING_PERCENT = new BigDecimal("80.0");

    public BudgetStatus {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(limit, "limit");
        Objects.requireNonNull(spent, "spent");
        Objects.requireNonNull(remaining, "remaining");
        Objects.requireNonNull(usedPercent, "usedPercent");
    }

    public static BudgetStatus of(Budget budget, BigDecimal spent) {
        BigDecimal normalizedSpent = Money.normalize(spent);
        BigDecimal remaining = Money.normalize(budget.limit().subtract(normalizedSpent));
        BigDecimal percent = normalizedSpent.multiply(BigDecimal.valueOf(100))
                .divide(budget.limit(), 1, RoundingMode.HALF_UP);
        return new BudgetStatus(budget.category(), budget.limit(), normalizedSpent, remaining, percent);
    }

    public BudgetState state() {
        if (remaining.signum() < 0) {
            return BudgetState.EXCEEDED;
        }
        if (remaining.signum() == 0) {
            return BudgetState.REACHED;
        }
        if (usedPercent.compareTo(WARNING_PERCENT) >= 0) {
            return BudgetState.WARNING;
        }
        return BudgetState.OK;
    }

    public BigDecimal overspend() {
        return remaining.signum() < 0 ? remaining.negate() : Money.ZERO;
    }
}
