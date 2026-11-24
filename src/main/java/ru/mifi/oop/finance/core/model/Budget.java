package ru.mifi.oop.finance.core.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A monthly-style limit assigned to one expense category.
 */
public record Budget(String category, BigDecimal limit) {

    public Budget {
        category = Objects.requireNonNull(category, "category").strip();
        Objects.requireNonNull(limit, "limit");
        limit = Money.positive(limit, "Лимит бюджета");
    }
}
