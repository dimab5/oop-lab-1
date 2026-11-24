package ru.mifi.oop.finance.core.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ru.mifi.oop.finance.TestSupport;
import ru.mifi.oop.finance.core.exception.DomainException;
import ru.mifi.oop.finance.core.exception.ValidationException;

class AuthServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void registersAndLogsInSeveralUsers() {
        TestSupport.Harness harness = TestSupport.harness(tempDir);
        harness.register("alice");
        harness.register("bob");

        assertEquals("alice", harness.login("alice").login());
        assertEquals("bob", harness.login("bob").login());
    }

    @Test
    void loginIsCaseInsensitiveAndStoredInLowerCase() {
        TestSupport.Harness harness = TestSupport.harness(tempDir);
        harness.register("Alice");

        assertEquals("alice", harness.login("ALICE").login());
    }

    @Test
    void rejectsDuplicateLogin() {
        TestSupport.Harness harness = TestSupport.harness(tempDir);
        harness.register("alice");

        assertThrows(DomainException.class, () -> harness.register("ALICE"));
    }

    @Test
    void rejectsWrongPasswordAndUnknownUser() {
        TestSupport.Harness harness = TestSupport.harness(tempDir);
        harness.register("alice");

        assertThrows(DomainException.class, () -> harness.auth().login("alice", "wrong-password"));
        assertThrows(DomainException.class, () -> harness.auth().login("unknown", TestSupport.PASSWORD));
    }

    @Test
    void rejectsInvalidLoginAndShortPassword() {
        TestSupport.Harness harness = TestSupport.harness(tempDir);

        assertThrows(ValidationException.class, () -> harness.auth().register("ab", TestSupport.PASSWORD));
        assertThrows(ValidationException.class, () -> harness.auth().register("alice", "123"));
        assertThrows(ValidationException.class, () -> harness.auth().register("alice", "      "));
    }

    @Test
    void storesOnlyPasswordHash() {
        TestSupport.Harness harness = TestSupport.harness(tempDir);
        harness.register("alice");

        UserAccount account = harness.users().findByLogin("alice").orElseThrow();
        assertNotEquals(TestSupport.PASSWORD, account.password().hash());
        assertTrue(account.password().hash().contains("hashed:"));
    }
}
