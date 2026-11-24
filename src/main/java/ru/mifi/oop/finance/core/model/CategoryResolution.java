package ru.mifi.oop.finance.core.model;

/**
 * Result of resolving a category by name: the stored category and whether it was just created.
 */
public record CategoryResolution(Category category, boolean created) {
}
