package ru.mifi.oop.finance.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ru.mifi.oop.finance.TestSupport;
import ru.mifi.oop.finance.core.exception.ValidationException;
import ru.mifi.oop.finance.core.model.DateRange;
import ru.mifi.oop.finance.core.model.TransactionType;
import ru.mifi.oop.finance.core.model.Wallet;
import ru.mifi.oop.finance.core.report.CategoryTotal;
import ru.mifi.oop.finance.core.report.WalletReport;

class ReportServiceTest {

    @TempDir
    Path tempDir;

    private TestSupport.Harness harness;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        harness = TestSupport.harness(tempDir);
        wallet = harness.walletFor("alice");
        harness.demo().load(wallet);
    }

    @Test
    void reportsExampleTotals() {
        WalletReport report = harness.reports().build(wallet, null, List.of(), null);

        assertEquals("63,000.00", MoneyFormat.amount(report.totalIncome()));
        assertEquals("8,300.00", MoneyFormat.amount(report.totalExpense()));
        assertEquals("54,700.00", MoneyFormat.amount(report.balance()));
        assertEquals(8, report.transactionCount());
    }

    @Test
    void reportsBudgetRemainingForEveryCategory() {
        WalletReport report = harness.reports().build(wallet, null, List.of(), null);

        assertEquals(3, report.budgets().size());
        assertTrue(report.budgets().stream().anyMatch(item ->
                item.category().equals("Коммунальные услуги")
                        && MoneyFormat.amount(item.remaining()).equals("-500.00")));
        assertTrue(report.budgets().stream().anyMatch(item ->
                item.category().equals("Еда")
                        && MoneyFormat.amount(item.remaining()).equals("3,200.00")));
        assertTrue(report.budgets().stream().anyMatch(item ->
                item.category().equals("Развлечения")
                        && MoneyFormat.amount(item.remaining()).equals("0.00")));
    }

    @Test
    void filtersBySeveralCategories() {
        WalletReport report = harness.reports().build(
                wallet, TransactionType.EXPENSE, List.of("Еда", "Такси"), null);

        assertEquals("2,300.00", MoneyFormat.amount(report.totalExpense()));
        assertEquals("0.00", MoneyFormat.amount(report.totalIncome()));
        assertEquals(List.of("Еда", "Такси"), report.expenseByCategory().stream()
                .map(CategoryTotal::category).toList());
    }

    @Test
    void reportsUnknownCategoryWithClearError() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> harness.reports().build(wallet, null, List.of("Еда", "Нет такой"), null));
        assertTrue(exception.getMessage().contains("Нет такой"));
    }

    @Test
    void filtersByPeriod() {
        // Demo operations are all created at TestSupport.NOW (2026-01-15).
        DateRange january = DateRange.ofLocalDates(
                java.time.LocalDate.of(2026, 1, 1),
                java.time.LocalDate.of(2026, 1, 31),
                TestSupport.CLOCK.getZone());
        DateRange february = DateRange.ofLocalDates(
                java.time.LocalDate.of(2026, 2, 1),
                java.time.LocalDate.of(2026, 2, 28),
                TestSupport.CLOCK.getZone());

        assertEquals("8,300.00", MoneyFormat.amount(
                harness.reports().build(wallet, null, List.of(), january).totalExpense()));
        assertEquals(0, harness.reports().build(wallet, null, List.of(), february).transactionCount());
    }

    @Test
    void periodFilterIncludesRangeBoundaries() {
        Wallet manual = new Wallet("bob", Instant.parse("2026-01-01T00:00:00Z"));
        manual.addIncome(new BigDecimal("100"), "Зарплата", "", Instant.parse("2026-01-31T23:59:59Z"));
        manual.addIncome(new BigDecimal("200"), "Зарплата", "", Instant.parse("2026-02-01T00:00:00Z"));

        DateRange january = DateRange.ofLocalDates(
                java.time.LocalDate.of(2026, 1, 1),
                java.time.LocalDate.of(2026, 1, 31),
                TestSupport.CLOCK.getZone());

        assertEquals("100.00", MoneyFormat.amount(
                harness.reports().build(manual, null, List.of(), january).totalIncome()));
    }

    @Test
    void filtersByType() {
        WalletReport report = harness.reports().build(wallet, TransactionType.INCOME, List.of(), null);

        assertTrue(report.expenseByCategory().isEmpty());
        assertEquals(2, report.incomeByCategory().size());
        assertEquals("63,000.00", MoneyFormat.amount(report.totalIncome()));
    }

    private static final class MoneyFormat {
        private MoneyFormat() {
        }

        static String amount(BigDecimal value) {
            return ru.mifi.oop.finance.core.model.Money.format(value);
        }
    }
}
