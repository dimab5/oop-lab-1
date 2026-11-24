package ru.mifi.oop.finance;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.mifi.oop.finance.core.auth.AuthService;
import ru.mifi.oop.finance.core.auth.HashedPassword;
import ru.mifi.oop.finance.core.auth.PasswordHasher;
import ru.mifi.oop.finance.core.auth.UserAccount;
import ru.mifi.oop.finance.core.model.Wallet;
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

/**
 * Test wiring with a deterministic clock and a fast (non-production) password hasher.
 */
public final class TestSupport {

    public static final Instant NOW = Instant.parse("2026-01-15T12:00:00Z");
    public static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    public static final String PASSWORD = "secret123";

    private TestSupport() {
    }

    public static Harness harness(Path directory) {
        ObjectMapper mapper = JsonMapperFactory.create();
        UserRepository users = new JsonUserRepository(directory, mapper);
        SnapshotCodec codec = new JsonSnapshotCodec(mapper);
        WalletSnapshotMapper mapperSnapshot = new WalletSnapshotMapper();
        WalletRepository wallets = new JsonWalletRepository(directory, codec, mapperSnapshot, CLOCK);
        PasswordHasher hasher = new PasswordHasher() {
            @Override
            public HashedPassword hash(String rawPassword) {
                return new HashedPassword("test-salt", "hashed:" + rawPassword);
            }

            @Override
            public boolean matches(String rawPassword, HashedPassword stored) {
                return stored.hash().equals("hashed:" + rawPassword);
            }
        };

        AuthService auth = new AuthService(users, hasher, CLOCK);
        NotificationService notifications = new NotificationService();
        FinanceService finance = new FinanceService(wallets, notifications, CLOCK);
        TransferService transfers = new TransferService(users, wallets, notifications, CLOCK);
        ReportService reports = new ReportService();
        SnapshotService snapshots = new SnapshotService(wallets, codec, mapperSnapshot, CLOCK);
        DemoService demo = new DemoService(finance, CLOCK);
        return new Harness(directory, users, wallets, auth, finance, transfers, reports, snapshots, demo);
    }

    public record Harness(
            Path directory,
            UserRepository users,
            WalletRepository wallets,
            AuthService auth,
            FinanceService finance,
            TransferService transfers,
            ReportService reports,
            SnapshotService snapshots,
            DemoService demo) {

        public UserAccount register(String login) {
            return auth.register(login, PASSWORD);
        }

        public Wallet walletFor(String login) {
            register(login);
            UserAccount user = auth.login(login, PASSWORD);
            return finance.openWallet(user);
        }

        public UserAccount login(String login) {
            return auth.login(login, PASSWORD);
        }
    }
}
