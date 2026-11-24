package ru.mifi.oop.finance.core.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import ru.mifi.oop.finance.core.exception.ValidationException;

/**
 * Money parsing, normalization and formatting. Amounts are always stored with two decimal places.
 */
public final class Money {

    public static final int SCALE = 2;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE);
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\+?\\d+(\\.\\d+)?");
    private static final String FIELD_AMOUNT = "Сумма";

    private Money() {
    }

    /**
     * Parses a positive amount. Both dot and comma are accepted as a decimal separator.
     *
     * @param raw user input
     * @return amount with scale 2
     */
    public static BigDecimal parsePositive(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Сумма не может быть пустой.");
        }
        String cleaned = raw.strip()
                .replace('\u00A0', ' ')
                .replace(" ", "")
                .replace(',', '.');
        if (cleaned.startsWith("-")) {
            throw new ValidationException("Сумма должна быть больше нуля.");
        }
        if (!AMOUNT_PATTERN.matcher(cleaned).matches()) {
            throw new ValidationException("Некорректная сумма «" + raw.strip()
                    + "». Укажите число, например 1500 или 1500.50.");
        }
        try {
            BigDecimal value = new BigDecimal(cleaned).stripTrailingZeros();
            if (value.scale() > SCALE) {
                throw new ValidationException("Сумма может содержать не более двух знаков после запятой.");
            }
            return positive(value, FIELD_AMOUNT);
        } catch (NumberFormatException exception) {
            throw new ValidationException("Некорректная сумма «" + raw.strip() + "».");
        }
    }

    /**
     * Validates that a value is a positive amount and normalizes it to scale 2.
     *
     * @param value amount
     * @param field human-readable field name
     * @return normalized amount
     */
    public static BigDecimal positive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field);
        if (value.signum() <= 0) {
            throw new ValidationException(field + " должна быть больше нуля.");
        }
        BigDecimal stripped = value.stripTrailingZeros();
        if (stripped.scale() > SCALE) {
            throw new ValidationException(field + " может содержать не более двух знаков после запятой.");
        }
        return value.setScale(SCALE, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal normalize(BigDecimal value) {
        Objects.requireNonNull(value, "value");
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Formats with a dot as decimal separator and a comma as thousands separator,
     * matching the format used in the assignment example: 63,000.00.
     */
    public static String format(BigDecimal value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(value);
    }

    public static String formatPercent(BigDecimal value) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        DecimalFormat format = new DecimalFormat("0.0", symbols);
        return format.format(value) + "%";
    }
}
