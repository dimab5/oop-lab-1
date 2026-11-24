package ru.mifi.oop.finance;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;

import ru.mifi.oop.finance.bootstrap.ApplicationFactory;
import ru.mifi.oop.finance.cli.CliApplication;
import ru.mifi.oop.finance.core.exception.AppException;

/**
 * Entry point. Usage: java -jar finance-wallet.jar [--data-dir <path>]
 */
public final class Main {

    private static final String DEFAULT_DATA_DIRECTORY = "data";

    private Main() {
    }

    public static void main(String[] args) {
        Path dataDirectory = Path.of(DEFAULT_DATA_DIRECTORY);
        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            if ("--help".equals(argument) || "-h".equals(argument)) {
                printUsage();
                return;
            }
            if ("--version".equals(argument)) {
                System.out.println("Finance Wallet CLI 1.0.0");
                return;
            }
            if ("--data-dir".equals(argument)) {
                if (index + 1 >= args.length) {
                    fail("После --data-dir укажите путь к папке данных.", 2);
                }
                dataDirectory = Path.of(args[++index]);
            } else if (argument.startsWith("--data-dir=")) {
                dataDirectory = Path.of(argument.substring("--data-dir=".length()));
            } else {
                fail("Неизвестный аргумент: " + argument + System.lineSeparator()
                        + "Использование: java -jar finance-wallet.jar [--data-dir <path>]", 2);
            }
        }

        Charset charset = Charset.defaultCharset();
        try {
            CliApplication application = ApplicationFactory.create(
                    dataDirectory,
                    new InputStreamReader(System.in, charset),
                    new PrintWriter(System.out, true, charset));
            int exitCode = application.run();
            if (exitCode != 0) {
                System.exit(exitCode);
            }
        } catch (AppException exception) {
            System.err.println("Не удалось запустить приложение: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("Finance Wallet CLI 1.0.0");
        System.out.println("Использование: java -jar finance-wallet.jar [--data-dir <path>]");
        System.out.println("После запуска введите help для списка команд.");
    }

    private static void fail(String message, int exitCode) {
        System.err.println(message);
        System.exit(exitCode);
    }
}
