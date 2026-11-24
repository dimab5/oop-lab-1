package ru.mifi.oop.finance.cli;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ru.mifi.oop.finance.core.model.BudgetStatus;
import ru.mifi.oop.finance.core.model.DateRange;
import ru.mifi.oop.finance.core.model.Money;
import ru.mifi.oop.finance.core.model.TransactionType;
import ru.mifi.oop.finance.core.report.CategoryTotal;
import ru.mifi.oop.finance.core.report.WalletReport;

/**
 * Presentation of reports and budget tables. All money formatting goes through {@link Money}.
 */
public final class OutputFormatter {

    private final Console console;
    private final Clock clock;

    public OutputFormatter(Console console, Clock clock) {
        this.console = console;
        this.clock = clock;
    }

    public void printSummary(WalletReport report) {
        console.line("Отчёт (" + periodLabel(report.period()) + "):");
        console.line("  Общий доход:    " + Money.format(report.totalIncome()));
        console.line("  Общие расходы: " + Money.format(report.totalExpense()));
        console.line("  Баланс:        " + Money.format(report.balance()));
        console.line("  Операций:      " + report.transactionCount());
        if (report.expensesExceedIncome()) {
            console.warning("расходы превышают доходы на "
                    + Money.format(report.totalExpense().subtract(report.totalIncome())) + ".");
        }
    }

    public void printIncomeCategories(WalletReport report) {
        printCategoryTable("Доходы по категориям:", report.incomeByCategory());
    }

    public void printExpenseCategories(WalletReport report) {
        printCategoryTable("Расходы по категориям:", report.expenseByCategory());
    }

    public void printCategoryBreakdown(WalletReport report) {
        if (report.incomeByCategory().isEmpty() && report.expenseByCategory().isEmpty()) {
            console.line("Нет операций по выбранным категориям и периоду.");
            return;
        }
        printIncomeCategories(report);
        printExpenseCategories(report);
    }

    public void printBudgets(List<BudgetStatus> statuses) {
        if (statuses.isEmpty()) {
            console.line("Бюджеты не установлены. Используйте: budget set <категория> <лимит>");
            return;
        }
        List<List<String>> rows = new ArrayList<>();
        for (BudgetStatus status : statuses) {
            rows.add(List.of(
                    status.category(),
                    Money.format(status.limit()),
                    Money.format(status.spent()),
                    Money.format(status.remaining()),
                    Money.formatPercent(status.usedPercent()),
                    status.state().label()));
        }
        console.line("Бюджеты по категориям:");
        console.table(new Table(
                List.of("Категория", "Лимит", "Потрачено", "Остаток", "Использовано", "Статус"),
                rows,
                Set.of(1, 2, 3, 4)));
    }

    public void printFull(WalletReport report) {
        printFull(report, null);
    }

    public void printFull(WalletReport report, TransactionType type) {
        printSummary(report);
        console.blankLine();
        printCategorySelection(report, type);
        console.blankLine();
        printBudgets(report.budgets());
    }

    public void printCategoriesOnly(WalletReport report) {
        printCategoriesOnly(report, null);
    }

    public void printCategoriesOnly(WalletReport report, TransactionType type) {
        printCategorySelection(report, type);
    }

    private void printCategorySelection(WalletReport report, TransactionType type) {
        if (type == TransactionType.INCOME) {
            printIncomeCategories(report);
        } else if (type == TransactionType.EXPENSE) {
            printExpenseCategories(report);
        } else {
            printCategoryBreakdown(report);
        }
    }

    private void printCategoryTable(String title, List<CategoryTotal> totals) {
        if (totals.isEmpty()) {
            console.line(title + " нет данных.");
            return;
        }
        List<List<String>> rows = new ArrayList<>();
        var total = Money.ZERO;
        for (CategoryTotal item : totals) {
            rows.add(List.of(item.category(), Money.format(item.amount())));
            total = total.add(item.amount());
        }
        rows.add(List.of("Итого", Money.format(total)));
        console.line(title);
        console.table(new Table(List.of("Категория", "Сумма"), rows, Set.of(1)));
    }

    private String periodLabel(DateRange range) {
        if (range == null || range.isAll()) {
            return "за всё время";
        }
        if (range.fromInclusive() != null && range.toExclusive() != null) {
            LocalDate from = range.fromInclusive().atZone(clock.getZone()).toLocalDate();
            LocalDate to = range.toExclusive().minusNanos(1).atZone(clock.getZone()).toLocalDate();
            return "с " + from + " по " + to;
        }
        if (range.fromInclusive() != null) {
            LocalDate from = range.fromInclusive().atZone(clock.getZone()).toLocalDate();
            return "с " + from;
        }
        LocalDate to = range.toExclusive().minusNanos(1).atZone(clock.getZone()).toLocalDate();
        return "по " + to;
    }
}
