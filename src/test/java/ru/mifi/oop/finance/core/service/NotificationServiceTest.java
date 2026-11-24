package ru.mifi.oop.finance.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import ru.mifi.oop.finance.core.model.BudgetState;
import ru.mifi.oop.finance.core.model.CategoryType;
import ru.mifi.oop.finance.core.model.Wallet;

class NotificationServiceTest {

    private final NotificationService service = new NotificationService();

    @Test
    void warnsWhenEightyPercentThresholdIsCrossed() {
        Wallet wallet = walletWithBudget("1000");
        wallet.addExpense(new BigDecimal("800"), "Еда", "", Instant.now());

        List<Notification> notifications = service.afterExpense(
                wallet, "Еда", BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("10000"));

        assertTrue(notifications.stream().anyMatch(item -> item.message().contains("80.0%")));
    }

    @Test
    void warnsWhenLimitIsExceeded() {
        Wallet wallet = walletWithBudget("1000");
        wallet.addExpense(new BigDecimal("1200"), "Еда", "", Instant.now());

        List<Notification> notifications = service.afterExpense(
                wallet, "Еда", BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("10000"));

        assertTrue(notifications.stream().anyMatch(item ->
                item.level() == NotificationLevel.DANGER && item.message().contains("превышен на 200.00")));
    }

    @Test
    void warnsWhenExpensesExceedIncome() {
        Wallet wallet = walletWithBudget("10000");
        wallet.addIncome(new BigDecimal("500"), "Зарплата", "", Instant.now());
        wallet.addExpense(new BigDecimal("600"), "Еда", "", Instant.now());

        List<Notification> notifications = service.afterExpense(
                wallet, "Еда", BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("500"));

        assertTrue(notifications.stream().anyMatch(item -> item.message().contains("Расходы превысили доходы")));
        assertTrue(notifications.stream().anyMatch(item -> item.message().contains("отрицательным")));
    }

    @Test
    void warnsWhenBalanceBecomesZero() {
        Wallet wallet = walletWithBudget("10000");
        wallet.addIncome(new BigDecimal("500"), "Зарплата", "", Instant.now());
        wallet.addExpense(new BigDecimal("500"), "Еда", "", Instant.now());

        List<Notification> notifications = service.afterExpense(
                wallet, "Еда", BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("500"));

        assertTrue(notifications.stream().anyMatch(item -> item.message().contains("равен нулю")));
    }

    @Test
    void currentWarningsSummarizeBudgetAndBalanceProblems() {
        Wallet wallet = walletWithBudget("1000");
        wallet.addExpense(new BigDecimal("1500"), "Еда", "", Instant.now());

        List<Notification> notifications = service.currentWarnings(wallet);

        assertTrue(notifications.stream().anyMatch(item -> item.message().contains("Бюджет")));
        assertTrue(notifications.stream().anyMatch(item -> item.message().contains("отрицательный")));
        assertEquals(BudgetState.EXCEEDED, wallet.budgetStatus("Еда").state());
    }

    private Wallet walletWithBudget(String limit) {
        Wallet wallet = new Wallet("alice", Instant.parse("2026-01-01T00:00:00Z"));
        wallet.addCategory("Еда", CategoryType.EXPENSE);
        wallet.setBudget("Еда", new BigDecimal(limit));
        return wallet;
    }
}
