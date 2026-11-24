package ru.mifi.oop.finance.cli.command;

import java.util.List;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;

/**
 * Saves the current wallet and ends the session.
 */
public final class LogoutCommand implements Command {

    @Override
    public String name() {
        return "logout";
    }

    @Override
    public String usage() {
        return "logout";
    }

    @Override
    public String summary() {
        return "Сохранить кошелёк в файл и выйти из текущей сессии.";
    }

    @Override
    public List<String> examples() {
        return List.of("logout");
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (!args.isEmpty()) {
            throw new CommandException("Использование: " + usage());
        }
        String login = context.session().requireUser().login();
        context.financeService().save(context.session().requireWallet());
        context.session().end();
        context.console().success("кошелёк «" + login + "» сохранён, сессия завершена.");
    }
}
