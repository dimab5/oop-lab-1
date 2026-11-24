package ru.mifi.oop.finance.core.service;

import ru.mifi.oop.finance.core.model.Category;

/**
 * Result of renaming a category, including how many operations were updated.
 */
public record CategoryRenameResult(Category category, long affectedOperations) {
}
