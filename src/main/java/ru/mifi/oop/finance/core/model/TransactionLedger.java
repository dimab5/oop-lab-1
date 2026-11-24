package ru.mifi.oop.finance.core.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import ru.mifi.oop.finance.core.exception.DomainException;

/**
 * All operations of a wallet. The ledger owns totals and filtering so reports do not
 * duplicate aggregation logic.
 */
public final class TransactionLedger {

    private final List<Transaction> transactions = new ArrayList<>();
    private final Set<UUID> ids = new HashSet<>();

    public void add(Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction");
        if (!ids.add(transaction.id())) {
            throw new DomainException("Операция с идентификатором " + transaction.id() + " уже существует.");
        }
        transactions.add(transaction);
    }

    public List<Transaction> all() {
        return List.copyOf(transactions);
    }

    public List<Transaction> select(TransactionType type, Set<String> categories, DateRange range) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (type != null && transaction.type() != type) {
                continue;
            }
            if (categories != null && !categories.isEmpty() && !categories.contains(transaction.category())) {
                continue;
            }
            if (range != null && !range.contains(transaction.occurredAt())) {
                continue;
            }
            result.add(transaction);
        }
        return Collections.unmodifiableList(result);
    }

    public BigDecimal total(TransactionType type, Set<String> categories, DateRange range) {
        BigDecimal total = Money.ZERO;
        for (Transaction transaction : select(type, categories, range)) {
            total = total.add(transaction.amount());
        }
        return Money.normalize(total);
    }

    public BigDecimal totalByCategory(String category, DateRange range) {
        return total(null, Set.of(category), range);
    }

    public Map<String, BigDecimal> totalsByCategory(TransactionType type, DateRange range) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (Transaction transaction : select(type, Set.of(), range)) {
            totals.merge(transaction.category(), transaction.amount(), BigDecimal::add);
        }
        return totals;
    }

    public long countByCategory(String category) {
        return transactions.stream().filter(item -> item.category().equals(category)).count();
    }

    public int size() {
        return transactions.size();
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    public void renameCategory(String oldName, String newName) {
        for (int index = 0; index < transactions.size(); index++) {
            Transaction transaction = transactions.get(index);
            if (transaction.category().equals(oldName)) {
                transactions.set(index, new Transaction(transaction.id(), transaction.type(), newName,
                        transaction.amount(), transaction.note(), transaction.occurredAt()));
            }
        }
    }

    public BigDecimal totalIncome(DateRange range) {
        return total(TransactionType.INCOME, Set.of(), range);
    }

    public BigDecimal totalExpense(DateRange range) {
        return total(TransactionType.EXPENSE, Set.of(), range);
    }

    public Instant lastOperationAt() {
        return transactions.isEmpty() ? null : transactions.get(transactions.size() - 1).occurredAt();
    }
}
