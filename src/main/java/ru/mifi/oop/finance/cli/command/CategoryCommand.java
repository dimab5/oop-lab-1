package ru.mifi.oop.finance.cli.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;
import ru.mifi.oop.finance.cli.Table;
import ru.mifi.oop.finance.core.model.Budget;
import ru.mifi.oop.finance.core.model.Category;
import ru.mifi.oop.finance.core.model.CategoryType;
import ru.mifi.oop.finance.core.model.Money;
import ru.mifi.oop.finance.core.model.Wallet;
import ru.mifi.oop.finance.core.service.CategoryRenameResult;

/**
 * Category management: add, list, rename and delete.
 */
public final class CategoryCommand implements Command {

    @Override
    public String name() {
        return "category";
    }

    @Override
    public String usage() {
        return "category <add|list|rename|delete> [аргументы]";
    }

    @Override
    public String summary() {
        return "Создавать, переименовывать и удалять категории доходов и расходов.";
    }

    @Override
    public List<String> examples() {
        return List.of(
                "category add Еда --type expense",
                "category add Зарплата --type income",
                "category list",
                "category rename Еда Продукты",
                "category delete Такси");
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (args.isEmpty()) {
            context.console().line("Использование: " + usage());
            context.console().line("Пример: category add Еда --type expense");
            return;
        }
        String action = args.get(0);
        switch (action) {
            case "add" -> add(context, args);
            case "list" -> list(context, args);
            case "rename" -> rename(context, args);
            case "delete" -> delete(context, args);
            default -> throw new CommandException("Неизвестное действие category: " + action
                    + ". Допустимо: add, list, rename, delete.");
        }
    }

    private void add(CommandContext context, List<String> args) {
        if (args.size() < 2) {
            throw new CommandException("Использование: category add <название> [--type expense|income]");
        }
        String name = args.get(1);
        CategoryType type = CategoryType.EXPENSE;
        int index = 2;
        while (index < args.size()) {
            if (!"--type".equals(args.get(index)) || index + 1 >= args.size()) {
                throw new CommandException("Использование: category add <название> [--type expense|income]");
            }
            type = CategoryType.from(args.get(index + 1));
            index += 2;
        }
        Category category = context.financeService().addCategory(context.session().requireWallet(), name, type);
        context.console().success("категория «" + category.name() + "» (" + category.type().label() + ") создана.");
    }

    private void list(CommandContext context, List<String> args) {
        if (args.size() != 1) {
            throw new CommandException("Использование: category list");
        }
        Wallet wallet = context.session().requireWallet();
        List<Category> categories = new ArrayList<>(wallet.categories().all());
        if (categories.isEmpty()) {
            context.console().line("Категорий пока нет. Добавьте: category add Еда --type expense");
            return;
        }
        categories.sort(Comparator.comparing(Category::name, String.CASE_INSENSITIVE_ORDER));
        List<List<String>> rows = new ArrayList<>();
        for (Category category : categories) {
            Optional<Budget> budget = wallet.budgets().find(category.name());
            rows.add(List.of(
                    category.name(),
                    category.type().label(),
                    Long.toString(wallet.operationCountInCategory(category.name())),
                    budget.map(value -> Money.format(value.limit())).orElse("—")));
        }
        context.console().line("Категории:");
        context.console().table(new Table(
                List.of("Категория", "Тип", "Операций", "Бюджет"), rows, Set.of(2, 3)));
    }

    private void rename(CommandContext context, List<String> args) {
        if (args.size() != 3) {
            throw new CommandException("Использование: category rename <старое> <новое>");
        }
        CategoryRenameResult result = context.financeService().renameCategory(
                context.session().requireWallet(), args.get(1), args.get(2));
        context.console().success("категория переименована в «" + result.category().name() + "»; обновлено операций: "
                + result.affectedOperations() + ".");
    }

    private void delete(CommandContext context, List<String> args) {
        if (args.size() != 2) {
            throw new CommandException("Использование: category delete <название>");
        }
        context.financeService().deleteCategory(context.session().requireWallet(), args.get(1));
        context.console().success("категория «" + args.get(1) + "» удалена.");
    }
}
