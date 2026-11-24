package ru.mifi.oop.finance.core.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import ru.mifi.oop.finance.core.exception.DomainException;
import ru.mifi.oop.finance.core.validation.Validators;

/**
 * Wallet aggregate: the owner, categories, operations and budgets belong together and
 * can only be changed through methods that protect their invariants.
 */
public final class Wallet {

    private final String ownerLogin;
    private final Instant createdAt;
    private final CategoryRegistry categories = new CategoryRegistry();
    private final TransactionLedger ledger = new TransactionLedger();
    private final BudgetBook budgets = new BudgetBook();

    public Wallet(String ownerLogin, Instant createdAt) {
        this.ownerLogin = Validators.login(ownerLogin);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }

    public static Wallet restore(
            String ownerLogin,
            Instant createdAt,
            Collection<Category> categories,
            Collection<Transaction> transactions,
            Collection<Budget> budgets) {
        Wallet wallet = new Wallet(ownerLogin, createdAt);
        for (Category category : categories) {
            wallet.categories.addExisting(category);
        }
        for (Transaction transaction : transactions) {
            Category category = wallet.categories.require(transaction.category(), transaction.type().categoryType());
            Transaction restored = new Transaction(transaction.id(), transaction.type(), category.name(),
                    transaction.amount(), transaction.note(), transaction.occurredAt());
            wallet.ledger.add(restored);
        }
        for (Budget budget : budgets) {
            Category category = wallet.categories.require(budget.category(), CategoryType.EXPENSE);
            wallet.budgets.put(category.name(), budget.limit());
        }
        return wallet;
    }

    public OperationResult addIncome(BigDecimal amount, String categoryName, String note, Instant occurredAt) {
        return addTransaction(TransactionType.INCOME, amount, categoryName, note, occurredAt);
    }

    public OperationResult addExpense(BigDecimal amount, String categoryName, String note, Instant occurredAt) {
        return addTransaction(TransactionType.EXPENSE, amount, categoryName, note, occurredAt);
    }

    private OperationResult addTransaction(
            TransactionType type,
            BigDecimal amount,
            String categoryName,
            String note,
            Instant occurredAt) {
        BigDecimal normalizedAmount = Money.positive(amount, "Сумма операции");
        CategoryResolution resolution = categories.ensure(categoryName, type.categoryType());
        Transaction transaction = Transaction.create(type, resolution.category().name(), normalizedAmount,
                Validators.note(note), occurredAt);
        ledger.add(transaction);
        return new OperationResult(transaction, resolution.created(), balance());
    }

    public Category addCategory(String name, CategoryType type) {
        return categories.add(name, type);
    }

    public Category renameCategory(String oldName, String newName) {
        Category old = categories.require(oldName);
        Category renamed = categories.rename(oldName, newName);
        if (!old.name().equals(renamed.name())) {
            budgets.renameCategory(old.name(), renamed.name());
            ledger.renameCategory(old.name(), renamed.name());
        }
        return renamed;
    }

    public void deleteCategory(String name) {
        Category category = categories.require(name);
        if (ledger.countByCategory(category.name()) > 0) {
            throw new DomainException("Нельзя удалить категорию «" + category.name()
                    + "»: по ней есть операции. Переименуйте её вместо удаления.");
        }
        if (budgets.contains(category.name())) {
            throw new DomainException("Нельзя удалить категорию «" + category.name()
                    + "»: для неё установлен бюджет. Сначала удалите бюджет.");
        }
        categories.remove(category.name());
    }

    public Budget setBudget(String categoryName, BigDecimal limit) {
        Category category = categories.require(categoryName, CategoryType.EXPENSE);
        return budgets.put(category.name(), limit);
    }

    public Budget removeBudget(String categoryName) {
        Category category = categories.require(categoryName);
        return budgets.remove(category.name());
    }

    public BudgetStatus budgetStatus(String categoryName) {
        Category category = categories.require(categoryName, CategoryType.EXPENSE);
        Budget budget = budgets.find(category.name())
                .orElseThrow(() -> new DomainException("Бюджет для категории «" + category.name() + "» не установлен."));
        BigDecimal spent = ledger.total(TransactionType.EXPENSE, Set.of(category.name()), DateRange.all());
        return BudgetStatus.of(budget, spent);
    }

    public List<BudgetStatus> budgetStatuses() {
        List<BudgetStatus> result = new ArrayList<>();
        for (Budget budget : budgets.all()) {
            BigDecimal spent = ledger.total(TransactionType.EXPENSE, Set.of(budget.category()), DateRange.all());
            result.add(BudgetStatus.of(budget, spent));
        }
        return result;
    }

    public BigDecimal totalIncome(DateRange range) {
        return ledger.totalIncome(range == null ? DateRange.all() : range);
    }

    public BigDecimal totalExpense(DateRange range) {
        return ledger.totalExpense(range == null ? DateRange.all() : range);
    }

    public BigDecimal balance() {
        return Money.normalize(totalIncome(DateRange.all()).subtract(totalExpense(DateRange.all())));
    }

    public boolean expensesExceedIncome() {
        return totalExpense(DateRange.all()).compareTo(totalIncome(DateRange.all())) > 0;
    }

    public Set<String> categoryNames(CategoryType type) {
        Set<String> names = new LinkedHashSet<>();
        for (Category category : categories.all()) {
            if (type == null || category.type() == type) {
                names.add(category.name());
            }
        }
        return names;
    }

    public boolean hasOperationInCategory(String categoryName) {
        return ledger.countByCategory(categoryName) > 0;
    }

    public long operationCountInCategory(String categoryName) {
        return ledger.countByCategory(categoryName);
    }

    public boolean isEmpty() {
        return categories.size() == 0 && ledger.isEmpty() && budgets.isEmpty();
    }

    public String ownerLogin() {
        return ownerLogin;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public CategoryRegistry categories() {
        return categories;
    }

    public TransactionLedger ledger() {
        return ledger;
    }

    public BudgetBook budgets() {
        return budgets;
    }
}
