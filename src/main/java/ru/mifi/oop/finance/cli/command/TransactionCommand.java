package ru.mifi.oop.finance.cli.command;

import java.math.BigDecimal;
import java.util.List;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandArguments;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;
import ru.mifi.oop.finance.core.model.Money;
import ru.mifi.oop.finance.core.model.TransactionType;
import ru.mifi.oop.finance.core.service.OperationOutcome;

/**
 * Adds an income or an expense. One class is used for both commands because the only
 * difference is the transaction type.
 */
public final class TransactionCommand implements Command {

    private final TransactionType type;

    public TransactionCommand(TransactionType type) {
        this.type = type;
    }

    @Override
    public String name() {
        return type == TransactionType.INCOME ? "income" : "expense";
    }

    @Override
    public String usage() {
        return name() + " <сумма> <категория> [--note <комментарий>]";
    }

    @Override
    public String summary() {
        return type == TransactionType.INCOME
                ? "Добавить доход. Категория создаётся автоматически, если её ещё нет."
                : "Добавить расход. Категория расходов создаётся автоматически, если её ещё нет.";
    }

    @Override
    public List<String> examples() {
        if (type == TransactionType.INCOME) {
            return List.of("income 20000 Зарплата", "income 1500,50 \"Подработка\" --note \"консультация\"");
        }
        return List.of("expense 300 Еда", "expense 1250,90 Такси --note \"поездка в аэропорт\"");
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (args.size() < 2) {
            throw new CommandException("Использование: " + usage());
        }
        BigDecimal amount = Money.parsePositive(args.get(0));
        String category = args.get(1);
        String note = CommandArguments.noteFrom(args, 2);

        OperationOutcome outcome;
        if (type == TransactionType.INCOME) {
            outcome = context.financeService().addIncome(context.session().requireWallet(), amount, category, note);
        } else {
            outcome = context.financeService().addExpense(context.session().requireWallet(), amount, category, note);
        }

        String label = type == TransactionType.INCOME ? "Доход" : "Расход";
        context.console().success(label + " добавлен: " + Money.format(amount)
                + " — " + outcome.result().transaction().category());
        if (outcome.result().categoryCreated()) {
            context.console().line("Создана категория «" + outcome.result().transaction().category()
                    + "» (" + type.categoryType().label() + ").");
        }
        if (!note.isEmpty()) {
            context.console().line("Комментарий: " + outcome.result().transaction().note());
        }
        context.console().line("Баланс: " + Money.format(outcome.result().balance()));
        context.console().notifications(outcome.notifications());
    }
}
