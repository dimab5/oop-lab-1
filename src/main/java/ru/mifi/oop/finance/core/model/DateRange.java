package ru.mifi.oop.finance.core.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

import ru.mifi.oop.finance.core.exception.ValidationException;

/**
 * Half-open time interval: {@code fromInclusive <= instant < toExclusive}.
 * A null boundary means "unbounded".
 */
public record DateRange(Instant fromInclusive, Instant toExclusive) {

    public DateRange {
        if (fromInclusive != null && toExclusive != null && !fromInclusive.isBefore(toExclusive)) {
            throw new ValidationException("Начало периода должно быть раньше его окончания.");
        }
    }

    public static DateRange all() {
        return new DateRange(null, null);
    }

    /**
     * Builds a range from user-facing dates. Both bounds are inclusive calendar days.
     */
    public static DateRange ofLocalDates(LocalDate from, LocalDate to, ZoneId zone) {
        Objects.requireNonNull(zone, "zone");
        if (from == null && to == null) {
            return all();
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания.");
        }
        Instant start = from == null ? null : from.atStartOfDay(zone).toInstant();
        Instant end = to == null ? null : to.plusDays(1).atStartOfDay(zone).toInstant();
        return new DateRange(start, end);
    }

    public boolean isAll() {
        return fromInclusive == null && toExclusive == null;
    }

    public boolean contains(Instant instant) {
        Objects.requireNonNull(instant, "instant");
        return (fromInclusive == null || !instant.isBefore(fromInclusive))
                && (toExclusive == null || instant.isBefore(toExclusive));
    }
}
