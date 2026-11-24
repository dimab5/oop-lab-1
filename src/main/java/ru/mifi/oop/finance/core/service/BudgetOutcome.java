package ru.mifi.oop.finance.core.service;

import java.util.List;

import ru.mifi.oop.finance.core.model.BudgetStatus;

/**
 * Budget state after setting a limit and related warnings.
 */
public record BudgetOutcome(BudgetStatus status, List<Notification> notifications) {

    public BudgetOutcome {
        notifications = List.copyOf(notifications);
    }
}
