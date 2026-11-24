package ru.mifi.oop.finance.cli.command;

import java.util.List;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;

/**
 * Explicitly persists the current wallet.
 */
public final class SaveCommand implements Command {

    @Override
    public String name() {
        return "save";
    }

    @Override
    public String usage() {
        return "save";
    }

    @Override
    public String summary() {
        return "Принудительно сохранить текущий кошелёк в файл.";
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (!args.isEmpty()) {
            throw new CommandException("Использование: " + usage());
        }
        context.financeService().save(context.session().requireWallet());
        context.console().success("кошелёк сохранён в " + context.dataDirectory().resolve("wallets"));
    }
}
