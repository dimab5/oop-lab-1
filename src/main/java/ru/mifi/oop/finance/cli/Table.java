package ru.mifi.oop.finance.cli;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Small ASCII table renderer with configurable right alignment.
 */
public final class Table {

    private final List<String> headers;
    private final List<List<String>> rows;
    private final Set<Integer> rightAligned;

    public Table(List<String> headers, List<List<String>> rows, Set<Integer> rightAlignedColumns) {
        this.headers = List.copyOf(headers);
        this.rows = new ArrayList<>();
        for (List<String> row : rows) {
            if (row.size() != headers.size()) {
                throw new IllegalArgumentException("Количество ячеек не совпадает с количеством колонок.");
            }
            this.rows.add(List.copyOf(row));
        }
        this.rightAligned = new HashSet<>(rightAlignedColumns);
    }

    public String render() {
        int columns = headers.size();
        int[] widths = new int[columns];
        for (int index = 0; index < columns; index++) {
            widths[index] = headers.get(index).length();
        }
        for (List<String> row : rows) {
            for (int index = 0; index < columns; index++) {
                widths[index] = Math.max(widths[index], row.get(index).length());
            }
        }

        String separator = separator(widths);
        StringBuilder result = new StringBuilder();
        result.append(separator).append(System.lineSeparator());
        result.append(row(headers, widths)).append(System.lineSeparator());
        result.append(separator).append(System.lineSeparator());
        for (List<String> row : rows) {
            result.append(row(row, widths)).append(System.lineSeparator());
        }
        result.append(separator);
        return result.toString();
    }

    private String separator(int[] widths) {
        StringBuilder result = new StringBuilder("+");
        for (int width : widths) {
            result.append("-".repeat(width + 2)).append("+");
        }
        return result.toString();
    }

    private String row(List<String> cells, int[] widths) {
        StringBuilder result = new StringBuilder("|");
        for (int index = 0; index < cells.size(); index++) {
            String cell = Objects.requireNonNull(cells.get(index), "cell");
            if (rightAligned.contains(index)) {
                result.append(' ').append(" ".repeat(widths[index] - cell.length())).append(cell).append(' ');
            } else {
                result.append(' ').append(cell).append(" ".repeat(widths[index] - cell.length())).append(' ');
            }
            result.append('|');
        }
        return result.toString();
    }
}
