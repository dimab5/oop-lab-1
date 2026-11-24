package ru.mifi.oop.finance.cli.command;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;
import ru.mifi.oop.finance.core.service.ExportResult;

/**
 * Exports the current wallet to a portable JSON snapshot.
 */
public final class ExportCommand implements Command {

    @Override
    public String name() {
        return "export";
    }

    @Override
    public String usage() {
        return "export <файл.json>";
    }

    @Override
    public String summary() {
        return "Экспортировать кошелёк в JSON-файл.";
    }

    @Override
    public List<String> examples() {
        return List.of("export backup.json", "export \"C:\\backups\\wallet.json\"");
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (args.size() != 1) {
            throw new CommandException("Использование: " + usage());
        }
        Path path = path(args.get(0));
        if (!path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")) {
            throw new CommandException("Файл экспорта должен иметь расширение .json.");
        }
        ExportResult result = context.snapshotService().export(context.session().requireWallet(), path);
        context.console().success("кошелёк экспортирован в " + result.path());
        context.console().line("Категорий: " + result.categories()
                + ", операций: " + result.transactions()
                + ", бюджетов: " + result.budgets() + ".");
    }

    private Path path(String raw) {
        try {
            return Path.of(raw);
        } catch (InvalidPathException exception) {
            throw new CommandException("Некорректный путь к файлу: " + raw);
        }
    }
}
