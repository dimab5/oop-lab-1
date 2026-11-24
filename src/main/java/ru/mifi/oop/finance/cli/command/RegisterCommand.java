package ru.mifi.oop.finance.cli.command;

import java.util.List;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;
import ru.mifi.oop.finance.core.auth.UserAccount;

/**
 * Registers a new user. Registration does not log the user in automatically.
 */
public final class RegisterCommand implements Command {

    @Override
    public String name() {
        return "register";
    }

    @Override
    public String usage() {
        return "register <логин> <пароль>";
    }

    @Override
    public String summary() {
        return "Зарегистрировать нового пользователя (логин 3–30 символов, пароль от 6 символов).";
    }

    @Override
    public List<String> examples() {
        return List.of("register alice secret123", "login alice secret123");
    }

    @Override
    public boolean requiresAuthentication() {
        return false;
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (context.session().isAuthenticated()) {
            throw new CommandException("Вы уже вошли. Для регистрации другого пользователя сначала выполните logout.");
        }
        if (args.size() != 2) {
            throw new CommandException("Использование: " + usage());
        }
        UserAccount account = context.authService().register(args.get(0), args.get(1));
        context.console().success("пользователь «" + account.login() + "» зарегистрирован.");
        context.console().line("Теперь войдите: login " + account.login() + " <пароль>");
    }
}
