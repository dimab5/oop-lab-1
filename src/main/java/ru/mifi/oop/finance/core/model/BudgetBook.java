package ru.mifi.oop.finance.core.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ru.mifi.oop.finance.core.exception.DomainException;

/**
 * Limits assigned to expense categories of one wallet.
 */
public final class BudgetBook {

    private final Map<String, Budget> budgets = new LinkedHashMap<>();

    public Budget put(String category, BigDecimal limit) {
        Budget budget = new Budget(category, limit);
        budgets.put(category, budget);
        return budget;
    }

    public Budget remove(String category) {
        Budget removed = budgets.remove(category);
        if (removed == null) {
            throw new DomainException("Бюджет для категории «" + category + "» не установлен.");
        }
        return removed;
    }

    public Optional<Budget> find(String category) {
        return Optional.ofNullable(budgets.get(category));
    }

    public boolean contains(String category) {
        return budgets.containsKey(category);
    }

    public Collection<Budget> all() {
        return List.copyOf(budgets.values());
    }

    public boolean isEmpty() {
        return budgets.isEmpty();
    }

    public int size() {
        return budgets.size();
    }

    public void renameCategory(String oldName, String newName) {
        Budget budget = budgets.remove(oldName);
        if (budget != null) {
            budgets.put(newName, new Budget(newName, budget.limit()));
        }
    }
}
