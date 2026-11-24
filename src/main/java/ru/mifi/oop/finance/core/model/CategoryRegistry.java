package ru.mifi.oop.finance.core.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import ru.mifi.oop.finance.core.exception.DomainException;
import ru.mifi.oop.finance.core.validation.Validators;

/**
 * Stores categories of one wallet. Names are unique case-insensitively while the original
 * spelling of the first created category is preserved for display.
 */
public final class CategoryRegistry {

    private final Map<String, Category> categories = new LinkedHashMap<>();

    public CategoryResolution ensure(String rawName, CategoryType type) {
        String name = Validators.categoryName(rawName);
        String key = key(name);
        Category existing = categories.get(key);
        if (existing != null) {
            if (existing.type() != type) {
                throw new DomainException("Категория «" + existing.name() + "» уже существует с типом «"
                        + existing.type().label() + "». Нельзя использовать её для операции типа «"
                        + type.label() + "».");
            }
            return new CategoryResolution(existing, false);
        }
        Category created = new Category(name, type);
        categories.put(key, created);
        return new CategoryResolution(created, true);
    }

    public Category add(String rawName, CategoryType type) {
        CategoryResolution resolution = ensure(rawName, type);
        if (!resolution.created()) {
            throw new DomainException("Категория «" + resolution.category().name() + "» уже существует.");
        }
        return resolution.category();
    }

    public void addExisting(Category category) {
        String key = key(category.name());
        if (categories.containsKey(key)) {
            throw new DomainException("Дубликат категории «" + category.name() + "».");
        }
        categories.put(key, category);
    }

    public Category require(String rawName) {
        String name = Validators.categoryName(rawName);
        Category category = categories.get(key(name));
        if (category == null) {
            throw new DomainException("Категория «" + name + "» не найдена. Создайте её командой category add.");
        }
        return category;
    }

    public Category require(String rawName, CategoryType expectedType) {
        Category category = require(rawName);
        if (category.type() != expectedType) {
            throw new DomainException("Категория «" + category.name() + "» относится к типу «"
                    + category.type().label() + "», а не «" + expectedType.label() + "».");
        }
        return category;
    }

    public Optional<Category> find(String rawName) {
        return Optional.ofNullable(categories.get(key(rawName.strip())));
    }

    public boolean contains(String rawName) {
        return categories.containsKey(key(rawName.strip()));
    }

    public Category rename(String rawOldName, String rawNewName) {
        String oldName = Validators.categoryName(rawOldName);
        String newName = Validators.categoryName(rawNewName);
        Category existing = require(oldName);
        String newKey = key(newName);
        Category conflict = categories.get(newKey);
        if (conflict != null && !conflict.name().equals(existing.name())) {
            throw new DomainException("Категория «" + newName + "» уже существует.");
        }
        if (existing.name().equals(newName)) {
            return existing;
        }
        categories.remove(key(oldName));
        Category renamed = new Category(newName, existing.type());
        categories.put(newKey, renamed);
        return renamed;
    }

    public void remove(String rawName) {
        Category category = require(rawName);
        categories.remove(key(category.name()));
    }

    public Collection<Category> all() {
        return java.util.List.copyOf(categories.values());
    }

    public int size() {
        return categories.size();
    }

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
