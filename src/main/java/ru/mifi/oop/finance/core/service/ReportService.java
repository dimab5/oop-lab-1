package ru.mifi.oop.finance.core.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.mifi.oop.finance.core.exception.ValidationException;
import ru.mifi.oop.finance.core.model.BudgetStatus;
import ru.mifi.oop.finance.core.model.Category;
import ru.mifi.oop.finance.core.model.CategoryType;
import ru.mifi.oop.finance.core.model.DateRange;
import ru.mifi.oop.finance.core.model.Money;
import ru.mifi.oop.finance.core.model.TransactionType;
import ru.mifi.oop.finance.core.model.Wallet;
import ru.mifi.oop.finance.core.report.CategoryTotal;
import ru.mifi.oop.finance.core.report.WalletReport;

/**
 * Pure reporting logic: totals, category breakdowns, budgets and period/category filtering.
 */
public final class ReportService {

    public WalletReport build(
            Wallet wallet,
            TransactionType type,
            Collection<String> categoryNames,
            DateRange requestedRange) {
        DateRange range = requestedRange == null ? DateRange.all() : requestedRange;
        Set<String> selected = resolveCategories(wallet, categoryNames);

        BigDecimal income = wallet.ledger().total(TransactionType.INCOME, selected, range);
        BigDecimal expense = wallet.ledger().total(TransactionType.EXPENSE, selected, range);
        int count = wallet.ledger().select(null, selected, range).size();

        List<CategoryTotal> incomeTotals = type == null || type == TransactionType.INCOME
                ? totals(wallet, CategoryType.INCOME, selected, range)
                : List.of();
        List<CategoryTotal> expenseTotals = type == null || type == TransactionType.EXPENSE
                ? totals(wallet, CategoryType.EXPENSE, selected, range)
                : List.of();

        return new WalletReport(
                Money.normalize(income),
                Money.normalize(expense),
                Money.normalize(income.subtract(expense)),
                incomeTotals,
                expenseTotals,
                sortedBudgets(wallet.budgetStatuses()),
                count,
                range);
    }

    public List<BudgetStatus> budgetReport(Wallet wallet) {
        return sortedBudgets(wallet.budgetStatuses());
    }

    private Set<String> resolveCategories(Wallet wallet, Collection<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return Set.of();
        }
        Set<String> selected = new LinkedHashSet<>();
        List<String> unknown = new ArrayList<>();
        for (String raw : categoryNames) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String name = raw.strip();
            var found = wallet.categories().find(name);
            if (found.isPresent()) {
                selected.add(found.get().name());
            } else {
                unknown.add(name);
            }
        }
        if (!unknown.isEmpty()) {
            throw new ValidationException("Категории не найдены: " + String.join(", ", unknown)
                    + ". Проверьте названия командой category list.");
        }
        if (selected.isEmpty()) {
            throw new ValidationException("Не указана ни одна категория для отчёта.");
        }
        return selected;
    }

    private List<CategoryTotal> totals(
            Wallet wallet,
            CategoryType categoryType,
            Set<String> selected,
            DateRange range) {
        TransactionType transactionType = categoryType == CategoryType.INCOME
                ? TransactionType.INCOME
                : TransactionType.EXPENSE;
        List<CategoryTotal> result = new ArrayList<>();
        for (Category category : wallet.categories().all()) {
            if (category.type() != categoryType) {
                continue;
            }
            if (!selected.isEmpty() && !selected.contains(category.name())) {
                continue;
            }
            BigDecimal total = wallet.ledger().total(transactionType, Set.of(category.name()), range);
            if (total.signum() != 0 || !selected.isEmpty()) {
                result.add(new CategoryTotal(category.name(), total));
            }
        }
        result.sort(Comparator.comparing(CategoryTotal::category, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private List<BudgetStatus> sortedBudgets(List<BudgetStatus> statuses) {
        List<BudgetStatus> result = new ArrayList<>(statuses);
        result.sort(Comparator.comparing(BudgetStatus::category, String.CASE_INSENSITIVE_ORDER));
        return result;
    }
}
