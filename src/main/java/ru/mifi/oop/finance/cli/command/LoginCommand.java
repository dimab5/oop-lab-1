package ru.mifi.oop.finance.cli.command;

import java.util.List;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;
import ru.mifi.oop.finance.core.auth.UserAccount;
import ru.mifi.oop.finance.core.model.Money;
import ru.mifi.oop.finance.core.model.Wallet;

/**
 * Authenticates a user and loads their wallet from disk (or creates a new empty wallet).
 */
public final class LoginCommand implements Command {

    @Override
    public String name() {
        return "login";
    }

    @Override
    public String usage() {
        return "login <логин> <пароль>";
    }

    @Override
    public String summary() {
        return "Войти и загрузить кошелёк пользователя из файла.";
    }

    @Override
    public List<String> examples() {
        return List.of("login alice secret123");
    }

    @Override
    public boolean requiresAuthentication() {
        return false;
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (context.session().isAuthenticated()) {
            throw new CommandException("Вы уже вошли как «" + context.session().requireUser().login()
                    + "». Сначала выполните logout.");
        }
        if (args.size() != 2) {
            throw new CommandException("Использование: " + usage());
        }
        UserAccount account = context.authService().login(args.get(0), args.get(1));
        Wallet wallet = context.financeService().openWallet(account);
        context.session().start(account, wallet);

        context.console().success("добро пожаловать, " + account.login() + "!");
        if (wallet.isEmpty()) {
            context.console().line("Кошелёк пуст. Добавьте доход или расход, например: income 50000 Зарплата");
        } else {
            context.console().line("Загружено операций: " + wallet.ledger().size()
                    + ", категорий: " + wallet.categories().size()
                    + ", бюджетов: " + wallet.budgets().size()
                    + ", баланс: " + Money.format(wallet.balance()) + ".");
        }
        context.console().notifications(context.financeService().currentWarnings(wallet));
    }
}
