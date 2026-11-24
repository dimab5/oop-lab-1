package ru.mifi.oop.finance.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import ru.mifi.oop.finance.core.exception.ValidationException;

class MoneyTest {

    @Test
    void parsesDotAndCommaDecimalSeparators() {
        assertEquals(new BigDecimal("1500.50"), Money.parsePositive("1 500,50"));
        assertEquals(new BigDecimal("1500.50"), Money.parsePositive("1500.5"));
        assertEquals(new BigDecimal("300.00"), Money.parsePositive("300"));
    }

    @Test
    void formatsUsingAssignmentExampleConvention() {
        assertEquals("63,000.00", Money.format(new BigDecimal("63000")));
        assertEquals("-500.00", Money.format(new BigDecimal("-500")));
        assertEquals("80.0%", Money.formatPercent(new BigDecimal("80.0")));
    }

    @Test
    void rejectsMoreThanTwoDecimalPlaces() {
        assertThrows(ValidationException.class, () -> Money.parsePositive("10.001"));
    }

    @Test
    void rejectsNegativeAndNonNumericValues() {
        assertThrows(ValidationException.class, () -> Money.parsePositive("-1"));
        assertThrows(ValidationException.class, () -> Money.parsePositive("abc"));
        assertThrows(ValidationException.class, () -> Money.parsePositive(" "));
    }
}
