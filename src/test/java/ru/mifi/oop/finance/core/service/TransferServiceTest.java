package ru.mifi.oop.finance.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ru.mifi.oop.finance.TestSupport;
import ru.mifi.oop.finance.core.exception.DomainException;
import ru.mifi.oop.finance.core.model.CategoryType;
import ru.mifi.oop.finance.core.model.TransactionType;
import ru.mifi.oop.finance.core.model.Wallet;

class TransferServiceTest {

    @TempDir
    Path tempDir;

    private TestSupport.Harness harness;
    private Wallet alice;

    @BeforeEach
    void setUp() {
        harness = TestSupport.harness(tempDir);
        alice = harness.walletFor("alice");
        harness.register("bob");
    }

    @Test
    void transferCreatesExpenseForSenderAndIncomeForRecipient() {
        harness.finance().addIncome(alice, new BigDecimal("1000"), "Зарплата", "");

        TransferResult result = harness.transfers().transfer(alice, "bob", new BigDecimal("300"), "долг");

        assertEquals(new BigDecimal("700.00"), alice.balance());
        assertEquals(TransactionType.EXPENSE, result.outgoing().type());
        assertEquals(TransactionType.INCOME, result.incoming().type());
        assertTrue(result.outgoing().note().contains("bob"));
        assertTrue(result.incoming().note().contains("alice"));

        Wallet bob = harness.wallets().findByOwner("bob").orElseThrow();
        assertEquals(new BigDecimal("300.00"), bob.balance());
        assertEquals(TransferService.INCOMING_CATEGORY, bob.ledger().all().get(0).category());
    }

    @Test
    void recipientWalletIsPersistedAndReloadedFromDisk() {
        harness.finance().addIncome(alice, new BigDecimal("1000"), "Зарплата", "");
        harness.transfers().transfer(alice, "bob", new BigDecimal("250"), "");

        TestSupport.Harness restarted = TestSupport.harness(tempDir);
        Wallet bob = restarted.wallets().findByOwner("bob").orElseThrow();
        assertEquals(new BigDecimal("250.00"), bob.balance());
    }

    @Test
    void rejectsUnknownRecipient() {
        harness.finance().addIncome(alice, new BigDecimal("1000"), "Зарплата", "");

        assertThrows(DomainException.class,
                () -> harness.transfers().transfer(alice, "nobody", new BigDecimal("100"), ""));
    }

    @Test
    void rejectsTransferToSelf() {
        harness.finance().addIncome(alice, new BigDecimal("1000"), "Зарплата", "");

        assertThrows(DomainException.class,
                () -> harness.transfers().transfer(alice, "alice", new BigDecimal("100"), ""));
    }

    @Test
    void rejectsInsufficientFunds() {
        harness.finance().addIncome(alice, new BigDecimal("100"), "Зарплата", "");

        assertThrows(DomainException.class,
                () -> harness.transfers().transfer(alice, "bob", new BigDecimal("101"), ""));
        assertEquals(new BigDecimal("100.00"), alice.balance());
    }

    @Test
    void recipientCategoryConflictDoesNotLeaveHalfCompletedTransfer() {
        harness.finance().addIncome(alice, new BigDecimal("1000"), "Зарплата", "");
        Wallet bob = harness.finance().openWallet(harness.login("bob"));
        harness.finance().addCategory(bob, TransferService.INCOMING_CATEGORY, CategoryType.EXPENSE);
        int senderOperationsBefore = alice.ledger().size();

        assertThrows(DomainException.class,
                () -> harness.transfers().transfer(alice, "bob", new BigDecimal("100"), ""));

        assertEquals(senderOperationsBefore, alice.ledger().size());
        assertEquals(new BigDecimal("1000.00"), alice.balance());
        assertTrue(alice.categories().find(TransferService.OUTGOING_CATEGORY).isEmpty());
    }
}
