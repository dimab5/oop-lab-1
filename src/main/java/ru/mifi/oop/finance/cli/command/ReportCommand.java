package ru.mifi.oop.finance.cli.command;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;
import ru.mifi.oop.finance.core.model.DateRange;
import ru.mifi.oop.finance.core.model.TransactionType;
import ru.mifi.oop.finance.core.report.WalletReport;

/**
 * Reports with optional period, type and category filters.
 */
public final class ReportCommand implements Command {

    @Override
    public String name() {
        return "report";
    }

    @Override
    public String usage() {
        return "report [summary|categories|budgets|all] [--type all|income|expense] "
                + "[--categories \"A,B\"] [--from YYYY-MM-DD] [--to YYYY-MM-DD]";
    }

    @Override
    public String summary() {
        return "Показать сводку, разбивку по категориям и состояние бюджетов.";
    }

    @Override
    public List<String> examples() {
        return List.of(
                "report",
                "report all",
                "report categories --type expense",
                "report categories --categories \"Еда,Такси\" --from 2026-01-01 --to 2026-01-31",
                "report budgets");
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        String section = "summary";
        int index = 0;
        if (!args.isEmpty() && !args.get(0).startsWith("--")) {
            section = args.get(0);
            index = 1;
        }
        if (!List.of("summary", "categories", "budgets", "all").contains(section)) {
            throw new CommandException("Неизвестный отчёт «" + section
                    + "». Допустимо: summary, categories, budgets, all.");
        }

        Query query = parse(args, index);
        if ("budgets".equals(section)) {
            if (query.hasFilters()) {
                throw new CommandException("Для report budgets фильтры не поддерживаются.");
            }
            context.formatter().printBudgets(context.reportService().budgetReport(context.session().requireWallet()));
            return;
        }

        WalletReport report = context.reportService().build(
                context.session().requireWallet(),
                query.type(),
                query.categories(),
                query.range(context));
        switch (section) {
            case "summary" -> context.formatter().printSummary(report);
            case "categories" -> context.formatter().printCategoriesOnly(report, query.type());
            case "all" -> context.formatter().printFull(report, query.type());
            default -> throw new CommandException("Неизвестный отчёт: " + section);
        }
    }

    private Query parse(List<String> args, int start) {
        TransactionType type = null;
        List<String> categories = new ArrayList<>();
        LocalDate from = null;
        LocalDate to = null;
        boolean filtered = false;
        int index = start;
        while (index < args.size()) {
            String option = args.get(index);
            if ("--type".equals(option)) {
                if (index + 1 >= args.size()) {
                    throw new CommandException("После --type укажите all, income или expense.");
                }
                type = parseType(args.get(index + 1));
                filtered = true;
                index += 2;
            } else if ("--categories".equals(option) || "--category".equals(option)) {
                if (index + 1 >= args.size()) {
                    throw new CommandException("После --categories укажите названия через запятую.");
                }
                for (String value : args.get(index + 1).split(",")) {
                    if (!value.isBlank()) {
                        categories.add(value.strip());
                    }
                }
                if (categories.isEmpty()) {
                    throw new CommandException("Список категорий пуст.");
                }
                filtered = true;
                index += 2;
            } else if ("--from".equals(option) || "--to".equals(option)) {
                if (index + 1 >= args.size()) {
                    throw new CommandException("После " + option + " укажите дату в формате YYYY-MM-DD.");
                }
                LocalDate date = parseDate(args.get(index + 1));
                if ("--from".equals(option)) {
                    from = date;
                } else {
                    to = date;
                }
                filtered = true;
                index += 2;
            } else {
                throw new CommandException("Неизвестный параметр отчёта: " + option);
            }
        }
        return new Query(type, List.copyOf(categories), from, to, filtered);
    }

    private TransactionType parseType(String raw) {
        if ("all".equalsIgnoreCase(raw)) {
            return null;
        }
        if ("income".equalsIgnoreCase(raw)) {
            return TransactionType.INCOME;
        }
        if ("expense".equalsIgnoreCase(raw)) {
            return TransactionType.EXPENSE;
        }
        throw new CommandException("Неизвестный тип отчёта «" + raw + "». Допустимо: all, income, expense.");
    }

    private LocalDate parseDate(String raw) {
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException exception) {
            throw new CommandException("Некорректная дата «" + raw + "». Используйте формат YYYY-MM-DD.");
        }
    }

    private record Query(
            TransactionType type,
            List<String> categories,
            LocalDate from,
            LocalDate to,
            boolean hasFilters) {

        DateRange range(CommandContext context) {
            return DateRange.ofLocalDates(from, to, context.clock().getZone());
        }
    }
}
