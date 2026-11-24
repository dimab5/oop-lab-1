package ru.mifi.oop.finance.core.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Set;

import ru.mifi.oop.finance.core.auth.UserAccount;
import ru.mifi.oop.finance.core.exception.DomainException;
import ru.mifi.oop.finance.core.model.Budget;
import ru.mifi.oop.finance.core.model.BudgetStatus;
import ru.mifi.oop.finance.core.model.Category;
import ru.mifi.oop.finance.core.model.CategoryType;
import ru.mifi.oop.finance.core.model.DateRange;
import ru.mifi.oop.finance.core.model.OperationResult;
import ru.mifi.oop.finance.core.model.TransactionType;
import ru.mifi.oop.finance.core.model.Wallet;
import ru.mifi.oop.finance.core.port.WalletRepository;
import ru.mifi.oop.finance.core.validation.Validators;

/**
 * Application service for authenticated wallet operations. It validates input, delegates
 * business rules to the {@link Wallet} aggregate, persists every change and asks the
 * notification service about crossed thresholds.
 */
public final class FinanceService {

    private final WalletRepository walletRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    public FinanceService(
            WalletRepository walletRepository,
            NotificationService notificationService,
            Clock clock) {
        this.walletRepository = walletRepository;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    public Wallet openWallet(UserAccount user) {
        return walletRepository.findByOwner(user.login())
                .orElseGet(() -> new Wallet(user.login(), clock.instant()));
    }

    public OperationOutcome addIncome(Wallet wallet, BigDecimal amount, String category, String note) {
        BigDecimal balanceBefore = wallet.balance();
        OperationResult result = wallet.addIncome(amount, category, note, clock.instant());
        save(wallet);
        List<Notification> notifications = notificationService.afterIncome(wallet, balanceBefore);
        return new OperationOutcome(result, notifications);
    }

    public OperationOutcome addExpense(Wallet wallet, BigDecimal amount, String category, String note) {
        String requestedCategory = Validators.categoryName(category);
        String categoryBefore = wallet.categories().find(requestedCategory)
                .map(Category::name)
                .orElse(requestedCategory);
        BigDecimal spentBefore = wallet.ledger().total(TransactionType.EXPENSE,
                Set.of(categoryBefore), DateRange.all());
        BigDecimal expensesBefore = wallet.totalExpense(DateRange.all());
        BigDecimal balanceBefore = wallet.balance();

        OperationResult result = wallet.addExpense(amount, requestedCategory, note, clock.instant());
        save(wallet);
        List<Notification> notifications = notificationService.afterExpense(
                wallet, result.transaction().category(), spentBefore, expensesBefore, balanceBefore);
        return new OperationOutcome(result, notifications);
    }

    public Category addCategory(Wallet wallet, String name, CategoryType type) {
        Category category = wallet.addCategory(name, type);
        save(wallet);
        return category;
    }

    public CategoryRenameResult renameCategory(Wallet wallet, String oldName, String newName) {
        Category old = wallet.categories().require(oldName);
        long affected = wallet.operationCountInCategory(old.name());
        Category renamed = wallet.renameCategory(old.name(), newName);
        save(wallet);
        return new CategoryRenameResult(renamed, affected);
    }

    public void deleteCategory(Wallet wallet, String name) {
        wallet.deleteCategory(name);
        save(wallet);
    }

    public BudgetOutcome setBudget(Wallet wallet, String category, BigDecimal limit) {
        Budget budget = wallet.setBudget(category, limit);
        save(wallet);
        BudgetStatus status = wallet.budgetStatus(budget.category());
        return new BudgetOutcome(status, notificationService.afterBudgetSet(wallet, budget.category()));
    }

    public Budget removeBudget(Wallet wallet, String category) {
        Budget removed = wallet.removeBudget(category);
        save(wallet);
        return removed;
    }

    public void save(Wallet wallet) {
        walletRepository.save(wallet);
    }

    public List<Notification> currentWarnings(Wallet wallet) {
        return notificationService.currentWarnings(wallet);
    }

    public Wallet requireWallet(Wallet wallet) {
        if (wallet == null) {
            throw new DomainException("Сначала выполните вход в систему.");
        }
        return wallet;
    }
}
