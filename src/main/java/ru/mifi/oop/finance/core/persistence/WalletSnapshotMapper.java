package ru.mifi.oop.finance.core.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ru.mifi.oop.finance.core.exception.AppException;
import ru.mifi.oop.finance.core.exception.PersistenceException;
import ru.mifi.oop.finance.core.model.Budget;
import ru.mifi.oop.finance.core.model.Category;
import ru.mifi.oop.finance.core.model.CategoryType;
import ru.mifi.oop.finance.core.model.Transaction;
import ru.mifi.oop.finance.core.model.TransactionType;
import ru.mifi.oop.finance.core.model.Wallet;
import ru.mifi.oop.finance.core.validation.Validators;

/**
 * Converts between the wallet aggregate and its portable snapshot.
 */
public final class WalletSnapshotMapper {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    public WalletSnapshot toSnapshot(Wallet wallet, Instant savedAt) {
        List<CategorySnapshot> categories = new ArrayList<>();
        for (Category category : wallet.categories().all()) {
            categories.add(new CategorySnapshot(category.name(), category.type().name()));
        }
        List<TransactionSnapshot> transactions = new ArrayList<>();
        for (Transaction transaction : wallet.ledger().all()) {
            transactions.add(new TransactionSnapshot(
                    transaction.id().toString(),
                    transaction.type().name(),
                    transaction.category(),
                    transaction.amount(),
                    transaction.note(),
                    transaction.occurredAt()));
        }
        List<BudgetSnapshot> budgets = new ArrayList<>();
        for (Budget budget : wallet.budgets().all()) {
            budgets.add(new BudgetSnapshot(budget.category(), budget.limit()));
        }
        return new WalletSnapshot(
                CURRENT_SCHEMA_VERSION,
                wallet.ownerLogin(),
                wallet.createdAt(),
                savedAt,
                categories,
                transactions,
                budgets);
    }

    public Wallet fromSnapshot(WalletSnapshot snapshot, String expectedOwner) {
        if (snapshot == null) {
            throw new PersistenceException("Файл кошелька пуст или имеет неверный формат.");
        }
        if (snapshot.schemaVersion() != CURRENT_SCHEMA_VERSION) {
            throw new PersistenceException("Неподдерживаемая версия формата кошелька: "
                    + snapshot.schemaVersion() + ". Ожидается " + CURRENT_SCHEMA_VERSION + ".");
        }
        String expected = Validators.login(expectedOwner);
        String actual;
        try {
            actual = Validators.login(snapshot.owner());
        } catch (AppException exception) {
            throw new PersistenceException("В снимке кошелька не указан корректный владелец.", exception);
        }
        if (!expected.equals(actual)) {
            throw new PersistenceException("Снимок принадлежит пользователю «" + actual
                    + "», а не «" + expected + "». Импорт чужого кошелька запрещён.");
        }

        try {
            List<Category> categories = new ArrayList<>();
            for (CategorySnapshot category : nullSafe(snapshot.categories())) {
                categories.add(new Category(category.name(), CategoryType.valueOf(category.type())));
            }
            List<Transaction> transactions = new ArrayList<>();
            for (TransactionSnapshot transaction : nullSafe(snapshot.transactions())) {
                transactions.add(new Transaction(
                        UUID.fromString(transaction.id()),
                        TransactionType.valueOf(transaction.type()),
                        transaction.category(),
                        amount(transaction.amount()),
                        transaction.note(),
                        transaction.occurredAt()));
            }
            List<Budget> budgets = new ArrayList<>();
            for (BudgetSnapshot budget : nullSafe(snapshot.budgets())) {
                budgets.add(new Budget(budget.category(), amount(budget.limit())));
            }
            Instant createdAt = snapshot.createdAt();
            if (createdAt == null) {
                throw new PersistenceException("В снимке отсутствует дата создания кошелька.");
            }
            return Wallet.restore(actual, createdAt, categories, transactions, budgets);
        } catch (PersistenceException exception) {
            throw exception;
        } catch (AppException | IllegalArgumentException | NullPointerException exception) {
            throw new PersistenceException("Некорректный снимок кошелька: " + exception.getMessage(), exception);
        }
    }

    private static BigDecimal amount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("сумма не указана");
        }
        return amount;
    }

    private static <T> List<T> nullSafe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
