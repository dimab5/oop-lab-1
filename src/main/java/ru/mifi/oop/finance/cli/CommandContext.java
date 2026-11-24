package ru.mifi.oop.finance.cli;

import java.nio.file.Path;
import java.time.Clock;

import ru.mifi.oop.finance.core.auth.AuthService;
import ru.mifi.oop.finance.core.service.DemoService;
import ru.mifi.oop.finance.core.service.FinanceService;
import ru.mifi.oop.finance.core.service.ReportService;
import ru.mifi.oop.finance.core.service.SnapshotService;
import ru.mifi.oop.finance.core.service.TransferService;

/**
 * Dependencies handed to every command. Commands never construct services themselves.
 */
public final class CommandContext {

    private final Console console;
    private final AppSession session;
    private final ApplicationState state;
    private final AuthService authService;
    private final FinanceService financeService;
    private final TransferService transferService;
    private final ReportService reportService;
    private final SnapshotService snapshotService;
    private final DemoService demoService;
    private final CommandRegistry registry;
    private final OutputFormatter formatter;
    private final Clock clock;
    private final Path dataDirectory;

    public CommandContext(
            Console console,
            AppSession session,
            ApplicationState state,
            AuthService authService,
            FinanceService financeService,
            TransferService transferService,
            ReportService reportService,
            SnapshotService snapshotService,
            DemoService demoService,
            CommandRegistry registry,
            OutputFormatter formatter,
            Clock clock,
            Path dataDirectory) {
        this.console = console;
        this.session = session;
        this.state = state;
        this.authService = authService;
        this.financeService = financeService;
        this.transferService = transferService;
        this.reportService = reportService;
        this.snapshotService = snapshotService;
        this.demoService = demoService;
        this.registry = registry;
        this.formatter = formatter;
        this.clock = clock;
        this.dataDirectory = dataDirectory;
    }

    public Console console() {
        return console;
    }

    public AppSession session() {
        return session;
    }

    public ApplicationState state() {
        return state;
    }

    public AuthService authService() {
        return authService;
    }

    public FinanceService financeService() {
        return financeService;
    }

    public TransferService transferService() {
        return transferService;
    }

    public ReportService reportService() {
        return reportService;
    }

    public SnapshotService snapshotService() {
        return snapshotService;
    }

    public DemoService demoService() {
        return demoService;
    }

    public CommandRegistry registry() {
        return registry;
    }

    public OutputFormatter formatter() {
        return formatter;
    }

    public Clock clock() {
        return clock;
    }

    public Path dataDirectory() {
        return dataDirectory;
    }
}
