package ru.mifi.oop.finance.cli.command;

import java.math.BigDecimal;
import java.util.List;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandArguments;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;
import ru.mifi.oop.finance.core.model.Money;
import ru.mifi.oop.finance.core.service.TransferResult;

/**
 * Transfer between registered users. The sender gets an expense and the recipient an income.
 */
public final class TransferCommand implements Command {

    @Override
    public String name() {
        return "transfer";
    }

    @Override
    public String usage() {
        return "transfer <логин получателя> <сумма> [--note <комментарий>]";
    }

    @Override
    public String summary() {
        return "Перевести деньги другому пользователю; операция фиксируется у обоих.";
    }

    @Override
    public List<String> examples() {
        return List.of(
                "transfer bob 1000",
                "transfer bob 250,50 --note \"возврат долга\"");
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (args.size() < 2) {
            throw new CommandException("Использование: " + usage());
        }
        BigDecimal amount = Money.parsePositive(args.get(1));
        String note = CommandArguments.noteFrom(args, 2);
        TransferResult result = context.transferService().transfer(
                context.session().requireWallet(), args.get(0), amount, note);
        context.console().success("перевод " + Money.format(result.amount())
                + " пользователю «" + result.recipientLogin() + "» выполнен.");
        context.console().line("Баланс отправителя: " + Money.format(result.senderBalance()));
        context.console().line("У получателя создан доход в категории «"
                + result.incoming().category() + "».");
        context.console().notifications(result.notifications());
    }
}
