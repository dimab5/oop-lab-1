package ru.mifi.oop.finance.core.service;

import java.nio.file.Path;

/**
 * Details of an exported wallet snapshot.
 */
public record ExportResult(Path path, int categories, int transactions, int budgets) {
}
