package ru.mifi.oop.finance.core.report;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Total amount for one category.
 */
public record CategoryTotal(String category, BigDecimal amount) {

    public CategoryTotal {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(amount, "amount");
    }
}
