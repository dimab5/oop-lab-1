package ru.mifi.oop.finance.infra.persistence;

import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ru.mifi.oop.finance.core.exception.PersistenceException;
import ru.mifi.oop.finance.core.persistence.WalletSnapshot;
import ru.mifi.oop.finance.core.port.SnapshotCodec;

/**
 * Jackson implementation of the snapshot codec.
 */
public final class JsonSnapshotCodec implements SnapshotCodec {

    private final ObjectMapper mapper;

    public JsonSnapshotCodec(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public WalletSnapshot read(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            throw new PersistenceException("Файл не найден: " + (path == null ? "" : path.toAbsolutePath()));
        }
        String content = JsonFiles.read(path);
        if (content.isBlank()) {
            throw new PersistenceException("Файл " + path.toAbsolutePath() + " пуст.");
        }
        try {
            return mapper.readValue(content, WalletSnapshot.class);
        } catch (JsonProcessingException exception) {
            throw new PersistenceException("Некорректный JSON в файле " + path.toAbsolutePath() + ": "
                    + exception.getOriginalMessage(), exception);
        }
    }

    @Override
    public void write(Path path, WalletSnapshot snapshot) {
        if (path == null) {
            throw new PersistenceException("Не указан путь для сохранения.");
        }
        try {
            String content = mapper.writeValueAsString(snapshot);
            JsonFiles.write(path, content);
        } catch (JsonProcessingException exception) {
            throw new PersistenceException("Не удалось сериализовать кошелёк: " + exception.getMessage(), exception);
        }
    }
}
