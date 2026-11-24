package ru.mifi.oop.finance.cli.command;

import java.util.List;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;

/**
 * Requests a graceful shutdown. The main loop performs the actual save and farewell.
 */
public final class ExitCommand implements Command {

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public String usage() {
        return "exit";
    }

    @Override
    public String summary() {
        return "Сохранить данные и выйти из приложения.";
    }

    @Override
    public boolean requiresAuthentication() {
        return false;
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (!args.isEmpty()) {
            throw new CommandException("Использование: exit");
        }
        context.state().requestExit();
    }
}
