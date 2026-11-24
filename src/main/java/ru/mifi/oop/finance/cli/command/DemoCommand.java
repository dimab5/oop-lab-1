package ru.mifi.oop.finance.cli.command;

import java.util.List;

import ru.mifi.oop.finance.cli.Command;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandException;
import ru.mifi.oop.finance.core.model.Wallet;
import ru.mifi.oop.finance.core.report.WalletReport;

/**
 * Loads the example from the assignment into an empty wallet.
 */
public final class DemoCommand implements Command {

    @Override
    public String name() {
        return "demo";
    }

    @Override
    public String usage() {
        return "demo";
    }

    @Override
    public String summary() {
        return "Заполнить пустой кошелёк примером из задания (доходы 63,000.00, расходы 8,300.00).";
    }

    @Override
    public List<String> examples() {
        return List.of("demo", "report all");
    }

    @Override
    public void execute(CommandContext context, List<String> args) {
        if (!args.isEmpty()) {
            throw new CommandException("Использование: " + usage());
        }
        Wallet wallet = context.session().requireWallet();
        context.demoService().load(wallet);
        context.console().success("демо-данные загружены.");
        WalletReport report = context.reportService().build(wallet, null, List.of(), null);
        context.formatter().printFull(report);
    }
}
