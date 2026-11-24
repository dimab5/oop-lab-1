package ru.mifi.oop.finance.core.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

import ru.mifi.oop.finance.core.exception.DomainException;
import ru.mifi.oop.finance.core.model.CategoryType;
import ru.mifi.oop.finance.core.model.Wallet;

/**
 * Fills an empty wallet with the example data from the assignment.
 */
public final class DemoService {

    private final FinanceService financeService;
    private final Clock clock;

    public DemoService(FinanceService financeService, Clock clock) {
        this.financeService = financeService;
        this.clock = clock;
    }

    public void load(Wallet wallet) {
        if (!wallet.isEmpty()) {
            throw new DomainException("Демо-данные можно загрузить только в пустой кошелёк.");
        }
        Instant start = clock.instant();
        wallet.addCategory("Еда", CategoryType.EXPENSE);
        wallet.addCategory("Развлечения", CategoryType.EXPENSE);
        wallet.addCategory("Коммунальные услуги", CategoryType.EXPENSE);
        wallet.addCategory("Такси", CategoryType.EXPENSE);
        wallet.addCategory("Зарплата", CategoryType.INCOME);
        wallet.addCategory("Бонус", CategoryType.INCOME);

        int step = 0;
        wallet.addExpense(new BigDecimal("300"), "Еда", "Обед", start.plusSeconds(step++));
        wallet.addExpense(new BigDecimal("500"), "Еда", "Продукты", start.plusSeconds(step++));
        wallet.addExpense(new BigDecimal("3000"), "Развлечения", "Кино и кафе", start.plusSeconds(step++));
        wallet.addExpense(new BigDecimal("3000"), "Коммунальные услуги", "Счета за месяц", start.plusSeconds(step++));
        wallet.addExpense(new BigDecimal("1500"), "Такси", "Поездки", start.plusSeconds(step++));
        wallet.addIncome(new BigDecimal("20000"), "Зарплата", "Аванс", start.plusSeconds(step++));
        wallet.addIncome(new BigDecimal("40000"), "Зарплата", "Основная часть", start.plusSeconds(step++));
        wallet.addIncome(new BigDecimal("3000"), "Бонус", "Квартальная премия", start.plusSeconds(step));

        wallet.setBudget("Еда", new BigDecimal("4000"));
        wallet.setBudget("Развлечения", new BigDecimal("3000"));
        wallet.setBudget("Коммунальные услуги", new BigDecimal("2500"));

        financeService.save(wallet);
    }
}
