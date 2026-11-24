package ru.mifi.oop.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ru.mifi.oop.finance.bootstrap.ApplicationFactory;
import ru.mifi.oop.finance.cli.CliApplication;

class CliIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void fullAssignmentScenarioProducesExpectedTotalsAndBudgets() {
        String output = run(
                "register alice secret123",
                "login alice secret123",
                "expense 300 Еда",
                "expense 500 Еда",
                "expense 3000 Развлечения",
                "expense 3000 \"Коммунальные услуги\"",
                "expense 1500 Такси",
                "income 20000 Зарплата",
                "income 40000 Зарплата",
                "income 3000 Бонус",
                "budget set Еда 4000",
                "budget set Развлечения 3000",
                "budget set \"Коммунальные услуги\" 2500",
                "report all",
                "exit").output();

        assertTrue(output.contains("Общий доход:    63,000.00"));
        assertTrue(output.contains("Общие расходы: 8,300.00"));
        assertTrue(output.contains("3,200.00"));
        assertTrue(output.contains("-500.00"));
        assertTrue(output.contains("0.00"));
        assertTrue(output.contains("Еда"));
        assertTrue(output.contains("Зарплата"));
        assertFalse(output.contains("внутренняя ошибка"));
    }

    @Test
    void walletPersistsBetweenApplicationRuns() {
        Result first = run(
                "register alice secret123",
                "login alice secret123",
                "income 1000 Зарплата",
                "exit");
        assertEquals(0, first.code());

        String second = run(
                "login alice secret123",
                "report",
                "exit").output();

        assertTrue(second.contains("Баланс:        1,000.00"));
        assertTrue(second.contains("Загружено операций: 1"));
    }

    @Test
    void commandsRequireAuthentication() {
        String output = run("expense 100 Еда", "exit").output();

        assertTrue(output.contains("сначала выполните вход"));
        assertTrue(output.contains("До свидания"));
    }

    @Test
    void helpAndUnknownCommandAreHandledWithoutCrash() {
        String output = run("help", "help expense", "unknown-command", "exit").output();

        assertTrue(output.contains("Finance Wallet CLI — команды"));
        assertTrue(output.contains("expense <сумма> <категория>"));
        assertTrue(output.contains("не найдена"));
        assertFalse(output.contains("внутренняя ошибка"));
    }

    @Test
    void usersCanTransferToEachOtherThroughCli() {
        String output = run(
                "register alice secret123",
                "register bob secret123",
                "login alice secret123",
                "income 1000 Зарплата",
                "transfer bob 300 --note \"долг\"",
                "logout",
                "login bob secret123",
                "report",
                "exit").output();

        assertTrue(output.contains("перевод 300.00 пользователю «bob» выполнен"));
        assertTrue(output.contains("Баланс:        300.00"));
        assertTrue(output.contains("Переводы (входящие)"));
    }

    @Test
    void exportAndImportWorkThroughCli() {
        Path exportFile = tempDir.resolve("wallet-export.json");
        run(
                "register alice secret123",
                "login alice secret123",
                "income 2500 Зарплата",
                "export \"" + exportFile + "\"",
                "exit");

        String output = run(
                "login alice secret123",
                "import \"" + exportFile + "\" --force",
                "report",
                "exit").output();

        assertTrue(output.contains("кошелёк импортирован"));
        assertTrue(output.contains("Баланс:        2,500.00"));
    }

    @Test
    void eofSavesWalletAndExitsNormally() {
        Result result = run(
                "register alice secret123",
                "login alice secret123",
                "income 700 Зарплата");

        assertEquals(0, result.code());
        assertTrue(result.output().contains("Ввод завершён"));
        String nextRun = run("login alice secret123", "report", "exit").output();
        assertTrue(nextRun.contains("Баланс:        700.00"));
    }

    @Test
    void validationErrorsAreReadable() {
        String output = run(
                "register alice secret123",
                "login alice secret123",
                "expense abc Еда",
                "expense 100",
                "budget set Неизвестная 1000",
                "exit").output();

        assertTrue(output.contains("Некорректная сумма"));
        assertTrue(output.contains("Использование: expense"));
        assertTrue(output.contains("не найдена"));
    }

    private Result run(String... commands) {
        String input = String.join(System.lineSeparator(), Arrays.asList(commands)) + System.lineSeparator();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        StringReader reader = new StringReader(input);
        PrintWriter writer = new PrintWriter(bytes, true, StandardCharsets.UTF_8);
        CliApplication application = ApplicationFactory.create(tempDir, reader, writer);
        int code = application.run();
        return new Result(code, bytes.toString(StandardCharsets.UTF_8));
    }

    private record Result(int code, String output) {
    }
}
