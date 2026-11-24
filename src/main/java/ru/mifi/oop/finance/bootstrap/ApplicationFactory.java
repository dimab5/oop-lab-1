package ru.mifi.oop.finance.bootstrap;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.time.Clock;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.mifi.oop.finance.cli.AppSession;
import ru.mifi.oop.finance.cli.ApplicationState;
import ru.mifi.oop.finance.cli.CliApplication;
import ru.mifi.oop.finance.cli.CommandContext;
import ru.mifi.oop.finance.cli.CommandRegistry;
import ru.mifi.oop.finance.cli.Console;
import ru.mifi.oop.finance.cli.OutputFormatter;
import ru.mifi.oop.finance.cli.command.BudgetCommand;
import ru.mifi.oop.finance.cli.command.CategoryCommand;
import ru.mifi.oop.finance.cli.command.DemoCommand;
import ru.mifi.oop.finance.cli.command.ExitCommand;
import ru.mifi.oop.finance.cli.command.ExportCommand;
import ru.mifi.oop.finance.cli.command.HelpCommand;
import ru.mifi.oop.finance.cli.command.ImportCommand;
import ru.mifi.oop.finance.cli.command.LoginCommand;
import ru.mifi.oop.finance.cli.command.LogoutCommand;
import ru.mifi.oop.finance.cli.command.ReportCommand;
import ru.mifi.oop.finance.cli.command.RegisterCommand;
import ru.mifi.oop.finance.cli.command.SaveCommand;
import ru.mifi.oop.finance.cli.command.TransactionCommand;
import ru.mifi.oop.finance.cli.command.TransferCommand;
import ru.mifi.oop.finance.cli.command.WhoamiCommand;
import ru.mifi.oop.finance.core.auth.AuthService;
import ru.mifi.oop.finance.core.model.TransactionType;
import ru.mifi.oop.finance.core.persistence.WalletSnapshotMapper;
import ru.mifi.oop.finance.core.port.SnapshotCodec;
import ru.mifi.oop.finance.core.port.UserRepository;
import ru.mifi.oop.finance.core.port.WalletRepository;
import ru.mifi.oop.finance.core.service.DemoService;
import ru.mifi.oop.finance.core.service.FinanceService;
import ru.mifi.oop.finance.core.service.NotificationService;
import ru.mifi.oop.finance.core.service.ReportService;
import ru.mifi.oop.finance.core.service.SnapshotService;
import ru.mifi.oop.finance.core.service.TransferService;
import ru.mifi.oop.finance.infra.persistence.JsonMapperFactory;
import ru.mifi.oop.finance.infra.persistence.JsonSnapshotCodec;
import ru.mifi.oop.finance.infra.persistence.JsonUserRepository;
import ru.mifi.oop.finance.infra.persistence.JsonWalletRepository;
import ru.mifi.oop.finance.infra.security.Pbkdf2PasswordHasher;

/**
 * Manual dependency injection root. Keeps Main and tests free from wiring details.
 */
public final class ApplicationFactory {

    private ApplicationFactory() {
    }

    public static CliApplication create(Path dataDirectory, Reader reader, Writer writer) {
        return create(dataDirectory, reader, writer, Clock.systemDefaultZone());
    }

    public static CliApplication create(Path dataDirectory, Reader reader, Writer writer, Clock clock) {
        Path dataPath = dataDirectory.toAbsolutePath().normalize();
        ObjectMapper mapper = JsonMapperFactory.create();
        UserRepository userRepository = new JsonUserRepository(dataPath, mapper);
        SnapshotCodec codec = new JsonSnapshotCodec(mapper);
        WalletSnapshotMapper snapshotMapper = new WalletSnapshotMapper();
        WalletRepository walletRepository = new JsonWalletRepository(dataPath, codec, snapshotMapper, clock);

        AuthService authService = new AuthService(userRepository, new Pbkdf2PasswordHasher(), clock);
        NotificationService notificationService = new NotificationService();
        FinanceService financeService = new FinanceService(walletRepository, notificationService, clock);
        TransferService transferService = new TransferService(
                userRepository, walletRepository, notificationService, clock);
        ReportService reportService = new ReportService();
        SnapshotService snapshotService = new SnapshotService(
                walletRepository, codec, snapshotMapper, clock);
        DemoService demoService = new DemoService(financeService, clock);

        Console console = new Console(reader, writer);
        AppSession session = new AppSession();
        ApplicationState state = new ApplicationState();
        CommandRegistry registry = new CommandRegistry();
        OutputFormatter formatter = new OutputFormatter(console, clock);

        CommandContext context = new CommandContext(
                console,
                session,
                state,
                authService,
                financeService,
                transferService,
                reportService,
                snapshotService,
                demoService,
                registry,
                formatter,
                clock,
                dataPath);

        registry.register(new HelpCommand());
        registry.register(new RegisterCommand());
        registry.register(new LoginCommand());
        registry.register(new LogoutCommand());
        registry.register(new WhoamiCommand());
        registry.register(new ExitCommand());
        registry.register(new TransactionCommand(TransactionType.INCOME));
        registry.register(new TransactionCommand(TransactionType.EXPENSE));
        registry.register(new TransferCommand());
        registry.register(new CategoryCommand());
        registry.register(new BudgetCommand());
        registry.register(new ReportCommand());
        registry.register(new ExportCommand());
        registry.register(new ImportCommand());
        registry.register(new SaveCommand());
        registry.register(new DemoCommand());
        registry.alias("quit", "exit");
        registry.alias("?", "help");

        return new CliApplication(context, console, dataPath);
    }
}
