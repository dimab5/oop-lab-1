package ru.mifi.oop.finance.infra.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import ru.mifi.oop.finance.core.exception.PersistenceException;

/**
 * Atomic UTF-8 file writes. A temporary file is used so a crash cannot leave a half-written wallet.
 */
public final class JsonFiles {

    private JsonFiles() {
    }

    public static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new PersistenceException("Не удалось прочитать файл " + path.toAbsolutePath() + ": "
                    + exception.getMessage(), exception);
        }
    }

    public static void write(Path path, String content) {
        Path absolute = path.toAbsolutePath().normalize();
        Path parent = absolute.getParent();
        if (parent == null) {
            throw new PersistenceException("Не удалось определить папку для файла " + absolute + ".");
        }
        try {
            Files.createDirectories(parent);
            Path temporary = Files.createTempFile(parent, absolute.getFileName().toString(), ".tmp");
            try {
                Files.writeString(temporary, content, StandardCharsets.UTF_8);
                move(temporary, absolute);
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException exception) {
            throw new PersistenceException("Не удалось сохранить файл " + absolute + ": "
                    + exception.getMessage(), exception);
        }
    }

    private static void move(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
