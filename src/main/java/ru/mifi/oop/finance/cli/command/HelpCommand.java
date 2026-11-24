package ru.mifi.oop.finance.cli.command;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;
import ru.mifi.oop.finance.cli.Table;

/**
 * Displays grouped command help and detailed help for one command.
 */
public final class HelpCommand implements Command {

    private static final Map<String, List<String>> GROUPS = groups();

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String usage() {
        return "help [команда]";
    }

    @Override
    public String summary() {
        return "Показать список команд или подробную справку по одной команде.";
    }

    @Override
    public boolean requiresAuthentication() {
        return false;
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (args.size() > 1) {
            throw new CommandException("Использование: " + usage());
        }
        if (args.size() == 1) {
            printOne(context, args.get(0));
            return;
        }
        context.console().line("Finance Wallet CLI — команды");
        context.console().line("Суммы вводятся положительными числами; поддерживаются 1500, 1500.50 и 1500,50.");
        context.console().line("Строки с пробелами заключайте в кавычки. Пример: expense 300 \"Еда\" --note \"обед\"");
        for (Map.Entry<String, List<String>> group : GROUPS.entrySet()) {
            context.console().blankLine();
            context.console().line(group.getKey() + ":");
            List<List<String>> rows = group.getValue().stream()
                    .map(context.registry()::find)
                    .flatMap(Optional::stream)
                    .map(command -> List.of(command.usage(), command.summary()))
                    .toList();
            if (!rows.isEmpty()) {
                context.console().table(new Table(List.of("Команда", "Описание"), rows, Set.of()));
            }
        }
        context.console().blankLine();
        context.console().line("Подробнее: help <команда>. Выход: exit (или Ctrl+D / EOF).");
    }

    private void printOne(CommandContext context, String name) {
        Optional<Command> found = context.registry().find(name);
        if (found.isEmpty()) {
            String suggestion = context.registry().suggestion(name);
            throw new CommandException("Команда «" + name + "» не найдена."
                    + (suggestion == null ? "" : " Возможно, вы имели в виду «" + suggestion + "»."));
        }
        Command command = found.get();
        context.console().line("Команда: " + command.name());
        context.console().line("Использование: " + command.usage());
        context.console().line(command.summary());
        if (!command.examples().isEmpty()) {
            context.console().blankLine();
            context.console().line("Примеры:");
            for (String example : command.examples()) {
                context.console().line("  " + example);
            }
        }
    }

    private static Map<String, List<String>> groups() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        result.put("Авторизация", List.of("register", "login", "logout", "whoami"));
        result.put("Операции", List.of("income", "expense", "transfer"));
        result.put("Категории и бюджеты", List.of("category", "budget"));
        result.put("Отчёты и данные", List.of("report", "export", "import", "save", "demo"));
        result.put("Система", List.of("help", "exit"));
        return result;
    }
}
