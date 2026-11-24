package ru.mifi.oop.finance.core.model;

/**
 * Health of a budget, used by reports, notifications and the CLI.
 */
public enum BudgetState {
    OK("в норме"),
    WARNING("близко к лимиту"),
    REACHED("лимит исчерпан"),
    EXCEEDED("превышен");

    private final String label;

    BudgetState(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
