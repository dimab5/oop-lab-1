package ru.mifi.oop.finance.cli.command;

import java.math.BigDecimal;
import java.util.List;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;
import ru.mifi.oop.finance.core.model.Budget;
import ru.mifi.oop.finance.core.model.Money;
import ru.mifi.oop.finance.core.service.BudgetOutcome;

/**
 * Budget management: set, list and remove limits for expense categories.
 */
public final class BudgetCommand implements Command {

    @Override
    public String name() {
        return "budget";
    }

    @Override
    public String usage() {
        return "budget <set|list|remove> [аргументы]";
    }

    @Override
    public String summary() {
        return "Установить, показать или удалить бюджет категории расходов.";
    }

    @Override
    public List<String> examples() {
        return List.of(
                "budget set Еда 4000",
                "budget list",
                "budget remove Еда");
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (args.isEmpty()) {
            context.console().line("Использование: " + usage());
            context.console().line("Пример: budget set Еда 4000");
            return;
        }
        String action = args.get(0);
        switch (action) {
            case "set" -> set(context, args);
            case "list" -> list(context, args);
            case "remove" -> remove(context, args);
            default -> throw new CommandException("Неизвестное действие budget: " + action
                    + ". Допустимо: set, list, remove.");
        }
    }

    private void set(CommandContext context, List<String> args) {
        if (args.size() != 3) {
            throw new CommandException("Использование: budget set <категория> <лимит>");
        }
        BigDecimal limit = Money.parsePositive(args.get(2));
        BudgetOutcome outcome = context.financeService().setBudget(
                context.session().requireWallet(), args.get(1), limit);
        context.console().success("бюджет «" + outcome.status().category() + "»: "
                + Money.format(outcome.status().limit()) + ".");
        context.console().line("Потрачено: " + Money.format(outcome.status().spent())
                + ", осталось: " + Money.format(outcome.status().remaining())
                + ", использовано: " + Money.formatPercent(outcome.status().usedPercent()) + ".");
        context.console().notifications(outcome.notifications());
    }

    private void list(CommandContext context, List<String> args) {
        if (args.size() != 1) {
            throw new CommandException("Использование: budget list");
        }
        context.formatter().printBudgets(context.reportService().budgetReport(context.session().requireWallet()));
    }

    private void remove(CommandContext context, List<String> args) {
        if (args.size() != 2) {
            throw new CommandException("Использование: budget remove <категория>");
        }
        Budget removed = context.financeService().removeBudget(
                context.session().requireWallet(), args.get(1));
        context.console().success("бюджет «" + removed.category() + "» ("
                + Money.format(removed.limit()) + ") удалён.");
    }
}
