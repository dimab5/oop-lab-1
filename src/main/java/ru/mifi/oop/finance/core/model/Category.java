package ru.mifi.oop.finance.core.model;

import java.util.Objects;

/**
 * A named category of income or expenses.
 */
public record Category(String name, CategoryType type) {

    public Category {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        name = name.strip();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Имя категории не может быть пустым.");
        }
    }
}
