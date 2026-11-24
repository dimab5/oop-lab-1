package ru.mifi.oop.finance.core.model;

import ru.mifi.oop.finance.core.exception.ValidationException;

/**
 * A category belongs either to income or to expenses. Budgets are allowed only for expenses.
 */
public enum CategoryType {
    INCOME("доход"),
    EXPENSE("расход");

    private final String label;

    CategoryType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static CategoryType from(String raw) {
        if (raw != null) {
            for (CategoryType type : values()) {
                if (type.name().equalsIgnoreCase(raw.strip())) {
                    return type;
                }
            }
        }
        throw new ValidationException("Неизвестный тип категории «" + raw
                + "». Допустимые значения: income, expense.");
    }
}
