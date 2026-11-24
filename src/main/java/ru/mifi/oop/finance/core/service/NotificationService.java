package ru.mifi.oop.finance.core.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.mifi.oop.finance.core.model.Budget;
import ru.mifi.oop.finance.core.model.BudgetState;
import ru.mifi.oop.finance.core.model.BudgetStatus;
import ru.mifi.oop.finance.core.model.DateRange;
import ru.mifi.oop.finance.core.model.Money;
import ru.mifi.oop.finance.core.model.TransactionType;
import ru.mifi.oop.finance.core.model.Wallet;

/**
 * Computes notifications only when a threshold is actually crossed, which prevents
 * repeating the same warning after every new operation.
 */
public final class NotificationService {

    public List<Notification> afterExpense(
            Wallet wallet,
            String category,
            BigDecimal spentBefore,
            BigDecimal expensesBefore,
            BigDecimal balanceBefore) {
        List<Notification> notifications = new ArrayList<>();
        BigDecimal spentAfter = wallet.ledger().total(
                TransactionType.EXPENSE,
                Set.of(category),
                DateRange.all());
        wallet.budgets().find(category).ifPresent(budget ->
                addBudgetNotifications(notifications, budget, spentBefore, spentAfter));

        BigDecimal income = wallet.totalIncome(DateRange.all());
        BigDecimal expensesAfter = wallet.totalExpense(DateRange.all());
        if (expensesBefore.compareTo(income) <= 0 && expensesAfter.compareTo(income) > 0) {
            notifications.add(Notification.danger("Расходы превысили доходы на "
                    + Money.format(expensesAfter.subtract(income)) + "."));
        }

        addBalanceNotifications(notifications, balanceBefore, wallet.balance());
        return notifications;
    }

    public List<Notification> afterIncome(Wallet wallet, BigDecimal balanceBefore) {
        List<Notification> notifications = new ArrayList<>();
        addBalanceNotifications(notifications, balanceBefore, wallet.balance());
        return notifications;
    }

    public List<Notification> afterBudgetSet(Wallet wallet, String category) {
        BudgetStatus status = wallet.budgetStatus(category);
        List<Notification> notifications = new ArrayList<>();
        if (status.state() == BudgetState.EXCEEDED) {
            notifications.add(Notification.danger("Лимит бюджета «" + category + "» уже превышен на "
                    + Money.format(status.overspend()) + "."));
        } else if (status.state() == BudgetState.REACHED) {
            notifications.add(Notification.warning("Лимит бюджета «" + category + "» уже исчерпан."));
        } else if (status.state() == BudgetState.WARNING) {
            notifications.add(Notification.warning("По категории «" + category + "» уже израсходовано "
                    + Money.formatPercent(status.usedPercent()) + " бюджета."));
        }
        return notifications;
    }

    public List<Notification> currentWarnings(Wallet wallet) {
        List<Notification> notifications = new ArrayList<>();
        for (BudgetStatus status : wallet.budgetStatuses()) {
            if (status.state() == BudgetState.EXCEEDED) {
                notifications.add(Notification.danger("Бюджет «" + status.category() + "» превышен на "
                        + Money.format(status.overspend()) + "."));
            } else if (status.state() == BudgetState.REACHED) {
                notifications.add(Notification.warning("Бюджет «" + status.category() + "» исчерпан."));
            } else if (status.state() == BudgetState.WARNING) {
                notifications.add(Notification.warning("Бюджет «" + status.category() + "» израсходован на "
                        + Money.formatPercent(status.usedPercent()) + "."));
            }
        }
        if (wallet.expensesExceedIncome()) {
            notifications.add(Notification.danger("Расходы превышают доходы на "
                    + Money.format(wallet.totalExpense(DateRange.all()).subtract(wallet.totalIncome(DateRange.all())))
                    + "."));
        }
        if (wallet.balance().signum() < 0) {
            notifications.add(Notification.danger("Баланс кошелька отрицательный: "
                    + Money.format(wallet.balance()) + "."));
        }
        return notifications;
    }

    private void addBudgetNotifications(
            List<Notification> notifications,
            Budget budget,
            BigDecimal spentBefore,
            BigDecimal spentAfter) {
        BigDecimal limit = budget.limit();
        boolean crossedLimit = spentBefore.compareTo(limit) < 0 && spentAfter.compareTo(limit) >= 0;
        if (crossedLimit) {
            if (spentAfter.compareTo(limit) > 0) {
                notifications.add(Notification.danger("Лимит бюджета «" + budget.category() + "» превышен на "
                        + Money.format(spentAfter.subtract(limit)) + ". Потрачено "
                        + Money.format(spentAfter) + " из " + Money.format(limit) + "."));
            } else {
                notifications.add(Notification.warning("Лимит бюджета «" + budget.category()
                        + "» исчерпан: потрачено " + Money.format(spentAfter) + " из " + Money.format(limit) + "."));
            }
            return;
        }

        BigDecimal warningThreshold = limit.multiply(BudgetStatus.WARNING_PERCENT)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        boolean crossedWarning = spentBefore.compareTo(warningThreshold) < 0
                && spentAfter.compareTo(warningThreshold) >= 0
                && spentAfter.compareTo(limit) < 0;
        if (crossedWarning) {
            BudgetStatus status = BudgetStatus.of(budget, spentAfter);
            notifications.add(Notification.warning("Израсходовано " + Money.formatPercent(status.usedPercent())
                    + " бюджета «" + budget.category() + "». Осталось "
                    + Money.format(status.remaining()) + "."));
        }
    }

    private void addBalanceNotifications(
            List<Notification> notifications,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter) {
        if (balanceBefore.signum() >= 0 && balanceAfter.signum() < 0) {
            notifications.add(Notification.danger("Баланс кошелька стал отрицательным: "
                    + Money.format(balanceAfter) + "."));
        } else if (balanceBefore.signum() != 0 && balanceAfter.signum() == 0) {
            notifications.add(Notification.warning("Баланс кошелька равен нулю."));
        } else if (balanceBefore.signum() < 0 && balanceAfter.signum() >= 0) {
            notifications.add(Notification.info("Баланс кошелька снова неотрицательный: "
                    + Money.format(balanceAfter) + "."));
        }
    }
}
