package ru.mifi.oop.finance.core.model;

/**
 * Type of a wallet operation.
 */
public enum TransactionType {
    INCOME("доход"),
    EXPENSE("расход");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public CategoryType categoryType() {
        return this == INCOME ? CategoryType.INCOME : CategoryType.EXPENSE;
    }
}
