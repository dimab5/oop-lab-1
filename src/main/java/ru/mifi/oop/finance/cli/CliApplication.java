package ru.mifi.oop.finance.cli;

import java.nio.file.Path;
import java.util.List;

import ru.mifi.oop.finance.core.exception.AppException;
import ru.mifi.oop.finance.core.model.Money;

/**
 * Reads commands in a loop, dispatches them and guarantees a final save on exit or EOF.
 */
public final class CliApplication {

    private final CommandContext context;
    private final Console console;
    private final Path dataDirectory;

    public CliApplication(CommandContext context, Console console, Path dataDirectory) {
        this.context = context;
        this.console = console;
        this.dataDirectory = dataDirectory;
    }

    public int run() {
        console.line("=== Finance Wallet CLI ===");
        console.line("Данные: " + dataDirectory);
        console.line("Введите help для списка команд, exit — для выхода.");
        while (!context.state().isExitRequested()) {
            console.prompt(context.session().prompt());
            String line = console.readLine();
            if (line == null) {
                console.blankLine();
                console.line("Ввод завершён, сохраняю данные.");
                break;
            }
            if (line.isBlank()) {
                continue;
            }
            try {
                dispatch(line);
            } catch (AppException exception) {
                console.error(exception.getMessage());
            } catch (RuntimeException exception) {
                console.error("внутренняя ошибка: " + exception.getMessage());
            }
        }
        return shutdown();
    }

    private void dispatch(String line) {
        List<String> tokens = CommandLine.tokenize(line);
        if (tokens.isEmpty()) {
            return;
        }
        String name = tokens.get(0);
        Command command = context.registry().find(name)
                .orElseThrow(() -> {
                    String suggestion = context.registry().suggestion(name);
                    return new CommandException("команда «" + name + "» не найдена."
                            + (suggestion == null ? "" : " Возможно, вы имели в виду «" + suggestion + "».")
                            + " Введите help.");
                });
        if (command.requiresAuthentication() && !context.session().isAuthenticated()) {
            throw new CommandException("сначала выполните вход: login <логин> <пароль>.");
        }
        command.execute(context, tokens.subList(1, tokens.size()));
    }

    private int shutdown() {
        int exitCode = 0;
        if (context.session().isAuthenticated()) {
            try {
                String login = context.session().requireUser().login();
                context.financeService().save(context.session().requireWallet());
                console.success("данные кошелька «" + login + "» сохранены. Баланс: "
                        + Money.format(context.session().requireWallet().balance()) + ".");
            } catch (AppException exception) {
                console.error("не удалось сохранить кошелёк при выходе: " + exception.getMessage());
                exitCode = 1;
            }
        }
        console.line("До свидания.");
        return exitCode;
    }
}
