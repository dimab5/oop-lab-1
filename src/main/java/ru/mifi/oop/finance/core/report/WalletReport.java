package ru.mifi.oop.finance.core.report;

import java.math.BigDecimal;
import java.util.List;

import ru.mifi.oop.finance.core.model.BudgetStatus;
import ru.mifi.oop.finance.core.model.DateRange;

/**
 * Complete report data for one wallet and one filter.
 */
public record WalletReport(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance,
        List<CategoryTotal> incomeByCategory,
        List<CategoryTotal> expenseByCategory,
        List<BudgetStatus> budgets,
        int transactionCount,
        DateRange period) {

    public WalletReport {
        incomeByCategory = List.copyOf(incomeByCategory);
        expenseByCategory = List.copyOf(expenseByCategory);
        budgets = List.copyOf(budgets);
    }

    public boolean expensesExceedIncome() {
        return totalExpense.compareTo(totalIncome) > 0;
    }

    public boolean isEmpty() {
        return transactionCount == 0;
    }
}
