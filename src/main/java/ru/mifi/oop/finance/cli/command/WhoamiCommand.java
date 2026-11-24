package ru.mifi.oop.finance.cli.command;

import java.util.List;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;
import ru.mifi.oop.finance.core.model.Money;

/**
 * Shows the current authenticated user and wallet summary.
 */
public final class WhoamiCommand implements Command {

    @Override
    public String name() {
        return "whoami";
    }

    @Override
    public String usage() {
        return "whoami";
    }

    @Override
    public String summary() {
        return "Показать текущего пользователя и краткую информацию о кошельке.";
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (!args.isEmpty()) {
            throw new CommandException("Использование: whoami");
        }
        var user = context.session().requireUser();
        var wallet = context.session().requireWallet();
        context.console().line("Пользователь: " + user.login());
        context.console().line("Кошелёк создан: " + wallet.createdAt());
        context.console().line("Операций: " + wallet.ledger().size());
        context.console().line("Баланс: " + Money.format(wallet.balance()));
    }
}
