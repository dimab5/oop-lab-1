package ru.mifi.oop.finance.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ru.mifi.oop.finance.TestSupport;
import ru.mifi.oop.finance.core.exception.DomainException;
import ru.mifi.oop.finance.core.exception.PersistenceException;
import ru.mifi.oop.finance.core.model.Wallet;

class SnapshotServiceTest {

    @TempDir
    Path tempDir;

    private TestSupport.Harness harness;
    private Wallet alice;

    @BeforeEach
    void setUp() {
        harness = TestSupport.harness(tempDir);
        alice = harness.walletFor("alice");
        harness.finance().addExpense(alice, new BigDecimal("300"), "Еда", "обед");
        harness.finance().setBudget(alice, "Еда", new BigDecimal("4000"));
        harness.finance().addIncome(alice, new BigDecimal("1000"), "Зарплата", "");
    }

    @Test
    void exportAndImportRoundTripPreservesWallet() {
        Path file = tempDir.resolve("backup.json");
        ExportResult export = harness.snapshots().export(alice, file);
        assertEquals(2, export.transactions());
        assertEquals(2, export.categories());

        Wallet imported = harness.snapshots().importSnapshot(alice, file, true);

        assertEquals(alice.balance(), imported.balance());
        assertEquals(alice.ledger().size(), imported.ledger().size());
        assertEquals(alice.budgets().size(), imported.budgets().size());
        assertEquals(new BigDecimal("3700.00"), imported.budgetStatus("Еда").remaining());
    }

    @Test
    void importRequiresForceForNonEmptyWallet() {
        Path file = tempDir.resolve("backup.json");
        harness.snapshots().export(alice, file);

        assertThrows(DomainException.class,
                () -> harness.snapshots().importSnapshot(alice, file, false));
    }

    @Test
    void importRejectsSnapshotOfAnotherUser() {
        Path file = tempDir.resolve("alice.json");
        harness.snapshots().export(alice, file);
        Wallet bob = harness.walletFor("bob");

        PersistenceException exception = assertThrows(PersistenceException.class,
                () -> harness.snapshots().importSnapshot(bob, file, true));
        assertTrue(exception.getMessage().contains("запрещён"));
    }

    @Test
    void importRejectsCorruptJson() throws Exception {
        Path file = tempDir.resolve("broken.json");
        Files.writeString(file, "{ this is not json");

        assertThrows(PersistenceException.class,
                () -> harness.snapshots().importSnapshot(alice, file, true));
    }

    @Test
    void explicitSaveIsLoadedByANewRepositoryInstance() {
        harness.finance().save(alice);

        TestSupport.Harness restarted = TestSupport.harness(tempDir);
        Wallet loaded = restarted.wallets().findByOwner("alice").orElseThrow();

        assertEquals(new BigDecimal("700.00"), loaded.balance());
        assertEquals(1, loaded.budgets().size());
    }
}
