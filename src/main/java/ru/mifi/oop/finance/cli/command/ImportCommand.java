package ru.mifi.oop.finance.cli.command;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;
import ru.mifi.oop.finance.core.model.Money;
import ru.mifi.oop.finance.core.model.Wallet;

/**
 * Imports a previously exported JSON snapshot, replacing the current wallet after validation.
 */
public final class ImportCommand implements Command {

    @Override
    public String name() {
        return "import";
    }

    @Override
    public String usage() {
        return "import <файл.json> [--force]";
    }

    @Override
    public String summary() {
        return "Импортировать JSON-снимок кошелька; --force нужен для замены непустого кошелька.";
    }

    @Override
    public List<String> examples() {
        return List.of("import backup.json", "import backup.json --force");
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        String rawPath = null;
        boolean force = false;
        for (String arg : args) {
            if ("--force".equals(arg)) {
                if (force) {
                    throw new CommandException("Флаг --force указан дважды.");
                }
                force = true;
            } else if (arg.startsWith("--")) {
                throw new CommandException("Неизвестный параметр: " + arg);
            } else if (rawPath == null) {
                rawPath = arg;
            } else {
                throw new CommandException("Использование: " + usage());
            }
        }
        if (rawPath == null) {
            throw new CommandException("Использование: " + usage());
        }
        Path path = path(rawPath);
        Wallet imported = context.snapshotService().importSnapshot(
                context.session().requireWallet(), path, force);
        context.session().replaceWallet(imported);
        context.console().success("кошелёк импортирован из " + path.toAbsolutePath().normalize());
        context.console().line("Операций: " + imported.ledger().size()
                + ", категорий: " + imported.categories().size()
                + ", бюджетов: " + imported.budgets().size()
                + ", баланс: " + Money.format(imported.balance()) + ".");
        context.console().notifications(context.financeService().currentWarnings(imported));
    }

    private Path path(String raw) {
        try {
            return Path.of(raw);
        } catch (InvalidPathException exception) {
            throw new CommandException("Некорректный путь к файлу: " + raw);
        }
    }
}
