package ru.mifi.oop.finance.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import ru.mifi.oop.finance.core.service.Notification;

/**
 * Thin wrapper around terminal input/output. It keeps command handlers independent of
 * System.in/System.out and makes CLI integration tests possible.
 */
public final class Console {

    private final BufferedReader reader;
    private final PrintWriter writer;

    public Console(Reader reader, Writer writer) {
        this.reader = new BufferedReader(reader);
        this.writer = new PrintWriter(writer, true);
    }

    public void print(String text) {
        writer.print(text);
        writer.flush();
    }

    public void line(String text) {
        writer.println(text);
        writer.flush();
    }

    public void blankLine() {
        writer.println();
        writer.flush();
    }

    public void prompt(String text) {
        writer.print(text);
        writer.flush();
    }

    public String readLine() {
        try {
            return reader.readLine();
        } catch (IOException exception) {
            throw new CommandException("Не удалось прочитать команду: " + exception.getMessage());
        }
    }

    public void error(String message) {
        writer.println("Ошибка: " + message);
        writer.flush();
    }

    public void success(String message) {
        writer.println("OK: " + message);
        writer.flush();
    }

    public void warning(String message) {
        writer.println("Внимание: " + message);
        writer.flush();
    }

    public void notifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            writer.println("[" + notification.level().marker() + "] " + notification.message());
        }
        if (!notifications.isEmpty()) {
            writer.flush();
        }
    }

    public void table(Table table) {
        writer.println(table.render());
        writer.flush();
    }
}
