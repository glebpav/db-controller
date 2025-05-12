package ru.mephi.db.infrastructure.db;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.adapter.db.LogRepository;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.LogUnableWriteTransactionException;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@AllArgsConstructor(onConstructor_ = @Inject)
public class LogRepositoryImpl implements LogRepository {
    private final Path logFilePath;

    @Override
    public void startTransaction(String transactionId) throws DatabaseException {
        try {
            writeLine("START TRANSACTION " + transactionId);
        } catch (IOException e) {
            throw new LogUnableWriteTransactionException("Unable to write transaction in log file");
        }
    }

    @Override
    public void writeChange(String transactionId, String pageId,
                            String beforeData, String afterData) throws IOException {
        // Пример: формат записи в лог:
        // TID=..., PAGE=..., BEFORE=..., AFTER=...
        String logEntry = String.format(
                "TID=%s, PAGE=%s, BEFORE=%s, AFTER=%s",
                transactionId, pageId, beforeData, afterData
        );
        writeLine(logEntry);
    }

    @Override
    public void commitTransaction(String transactionId) throws IOException {
        writeLine("COMMIT " + transactionId);
    }

    @Override
    public void rollbackTransaction(String transactionId) throws IOException {
        writeLine("ROLLBACK " + transactionId);
    }

    private void writeLine(String line) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(
                logFilePath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            bw.write(line);
            bw.newLine();
            bw.flush();
        }
    }

}
