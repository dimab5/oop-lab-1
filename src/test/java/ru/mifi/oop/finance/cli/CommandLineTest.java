package ru.mifi.oop.finance.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class CommandLineTest {

    @Test
    void splitsQuotedArgumentsAndKeepsSpaces() {
        List<String> tokens = CommandLine.tokenize("expense 300 \"Еда\" --note 'обед с коллегами'");
        assertEquals(List.of("expense", "300", "Еда", "--note", "обед с коллегами"), tokens);
    }

    @Test
    void preservesWindowsBackslashesInPaths() {
        List<String> tokens = CommandLine.tokenize("export \"C:\\backups\\wallet.json\"");
        assertEquals(List.of("export", "C:\\backups\\wallet.json"), tokens);
    }

    @Test
    void preservesUncNetworkPaths() {
        List<String> tokens = CommandLine.tokenize("import \"\\\\server\\share\\wallet.json\"");
        assertEquals(List.of("import", "\\\\server\\share\\wallet.json"), tokens);
    }

    @Test
    void supportsEmptyQuotedArgument() {
        assertEquals(List.of("note", ""), CommandLine.tokenize("note \"\""));
    }

    @Test
    void rejectsUnclosedQuote() {
        assertThrows(CommandException.class, () -> CommandLine.tokenize("expense 100 \"Еда"));
    }
}
