package ru.mifi.oop.finance.cli;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a command line into arguments. Supports single and double quotes, so notes and
 * file paths with spaces can be passed as one argument.
 */
public final class CommandLine {

    private CommandLine() {
    }

    public static List<String> tokenize(String line) {
        if (line == null) {
            return List.of();
        }
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean tokenStarted = false;
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean escaped = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            char next = index + 1 < line.length() ? line.charAt(index + 1) : '\0';

            if (escaped) {
                current.append(character);
                tokenStarted = true;
                escaped = false;
            } else if (character == '\\' && !singleQuoted && next == '"') {
                // Only a double quote is escaped. All other backslashes are preserved,
                // which keeps Windows paths such as C:\data and \\server\share intact.
                escaped = true;
                tokenStarted = true;
            } else if (character == '"' && !singleQuoted) {
                doubleQuoted = !doubleQuoted;
                tokenStarted = true;
            } else if (character == '\'' && !doubleQuoted) {
                singleQuoted = !singleQuoted;
                tokenStarted = true;
            } else if (Character.isWhitespace(character) && !singleQuoted && !doubleQuoted) {
                if (tokenStarted) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    tokenStarted = false;
                }
            } else {
                current.append(character);
                tokenStarted = true;
            }
        }

        if (escaped) {
            current.append('\\');
        }
        if (singleQuoted || doubleQuoted) {
            throw new CommandException("Незакрытая кавычка в команде.");
        }
        if (tokenStarted) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
