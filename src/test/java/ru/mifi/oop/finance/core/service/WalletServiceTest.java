package ru.mifi.oop.finance.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ru.mifi.oop.finance.TestSupport;
import ru.mifi.oop.finance.core.exception.DomainException;
import ru.mifi.oop.finance.core.exception.ValidationException;
import ru.mifi.oop.finance.core.model.BudgetStatus;
import ru.mifi.oop.finance.core.model.CategoryType;
import ru.mifi.oop.finance.core.model.Money;
import ru.mifi.oop.finance.core.model.TransactionType;
import ru.mifi.oop.finance.core.model.Wallet;

class WalletServiceTest {

    @TempDir
    Path tempDir;

    private TestSupport.Harness harness;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        harness = TestSupport.harness(tempDir);
        wallet = harness.walletFor("alice");
    }

    @Test
    void incomeAndExpenseUpdateBalanceAndTotals() {
        harness.finance().addIncome(wallet, new BigDecimal("1000"), "Зарплата", "");
        harness.finance().addExpense(wallet, new BigDecimal("250.50"), "Еда", "обед");

        assertEquals(new BigDecimal("1000.00"), wallet.totalIncome(null));
        assertEquals(new BigDecimal("250.50"), wallet.totalExpense(null));
        assertEquals(new BigDecimal("749.50"), wallet.balance());
        assertEquals(2, wallet.ledger().size());
        assertEquals(1, wallet.ledger().select(TransactionType.EXPENSE, null, null).size());
    }

    @Test
    void automaticallyCreatesCategoryForOperation() {
        OperationOutcome outcome = harness.finance().addExpense(
                wallet, new BigDecimal("300"), "Еда", "");

        assertTrue(outcome.result().categoryCreated());
        assertTrue(wallet.categories().contains("Еда"));
        assertEquals(CategoryType.EXPENSE, wallet.categories().require("Еда").type());
    }

    @Test
    void rejectsZeroNegativeAndTooPreciseAmounts() {
        assertThrows(ValidationException.class,
                () -> harness.finance().addIncome(wallet, BigDecimal.ZERO, "Зарплата", ""));
        assertThrows(ValidationException.class,
                () -> harness.finance().addExpense(wallet, new BigDecimal("-1"), "Еда", ""));
        assertThrows(ValidationException.class,
                () -> harness.finance().addExpense(wallet, new BigDecimal("1.001"), "Еда", ""));
    }

    @Test
    void rejectsCategoryTypeConflict() {
        harness.finance().addCategory(wallet, "Зарплата", CategoryType.INCOME);

        assertThrows(DomainException.class,
                () -> harness.finance().addExpense(wallet, new BigDecimal("100"), "Зарплата", ""));
    }

    @Test
    void setsBudgetAndCalculatesRemainingLimit() {
        harness.finance().addExpense(wallet, new BigDecimal("800"), "Еда", "");
        BudgetOutcome outcome = harness.finance().setBudget(wallet, "Еда", new BigDecimal("4000"));

        assertEquals(new BigDecimal("4000.00"), outcome.status().limit());
        assertEquals(new BigDecimal("800.00"), outcome.status().spent());
        assertEquals(new BigDecimal("3200.00"), outcome.status().remaining());
        assertEquals(new BigDecimal("20.0"), outcome.status().usedPercent());
    }

    @Test
    void budgetRequiresExistingExpenseCategory() {
        assertThrows(DomainException.class,
                () -> harness.finance().setBudget(wallet, "Еда", new BigDecimal("4000")));

        harness.finance().addCategory(wallet, "Зарплата", CategoryType.INCOME);
        assertThrows(DomainException.class,
                () -> harness.finance().setBudget(wallet, "Зарплата", new BigDecimal("4000")));
    }

    @Test
    void renameUpdatesOperationsAndBudget() {
        harness.finance().addExpense(wallet, new BigDecimal("300"), "Еда", "");
        harness.finance().setBudget(wallet, "Еда", new BigDecimal("1000"));

        CategoryRenameResult result = harness.finance().renameCategory(wallet, "Еда", "Продукты");

        assertEquals("Продукты", result.category().name());
        assertEquals(1, result.affectedOperations());
        assertEquals("Продукты", wallet.ledger().all().get(0).category());
        assertEquals(new BigDecimal("1000.00"), wallet.budgetStatus("Продукты").limit());
        assertFalse(wallet.categories().contains("Еда"));
    }

    @Test
    void cannotDeleteCategoryWithOperationsOrBudget() {
        harness.finance().addExpense(wallet, new BigDecimal("300"), "Еда", "");
        assertThrows(DomainException.class, () -> harness.finance().deleteCategory(wallet, "Еда"));
    }

    @Test
    void deletesUnusedCategory() {
        harness.finance().addCategory(wallet, "Такси", CategoryType.EXPENSE);

        harness.finance().deleteCategory(wallet, "Такси");

        assertFalse(wallet.categories().contains("Такси"));
    }

    @Test
    void warnsAtEightyPercentOfBudget() {
        harness.finance().addCategory(wallet, "Еда", CategoryType.EXPENSE);
        harness.finance().setBudget(wallet, "Еда", new BigDecimal("1000"));

        OperationOutcome outcome = harness.finance().addExpense(wallet, new BigDecimal("800"), "Еда", "");

        assertTrue(outcome.notifications().stream()
                .anyMatch(item -> item.message().contains("80.0%")));
    }

    @Test
    void savesWalletToDiskAfterEachMutation() throws Exception {
        harness.finance().addIncome(wallet, new BigDecimal("500"), "Зарплата", "");

        assertTrue(Files.isDirectory(tempDir.resolve("wallets")));
        try (var files = Files.list(tempDir.resolve("wallets"))) {
            assertTrue(files.findAny().isPresent());
        }
        Wallet reloaded = harness.wallets().findByOwner("alice").orElseThrow();
        assertEquals(new BigDecimal("500.00"), reloaded.balance());
        assertEquals(List.of("Зарплата"), reloaded.ledger().all().stream()
                .map(item -> item.category()).toList());
    }

    @Test
    void budgetStatusUsesMoneyFormatting() {
        harness.finance().addExpense(wallet, new BigDecimal("3000"), "Коммунальные услуги", "");
        BudgetOutcome outcome = harness.finance().setBudget(
                wallet, "Коммунальные услуги", new BigDecimal("2500"));

        BudgetStatus status = outcome.status();
        assertEquals("-500.00", Money.format(status.remaining()));
        assertEquals("превышен", status.state().label());
    }
}
