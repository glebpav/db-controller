package ru.mephi.db.application.usecase;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.adapter.io.IOUtils;
import ru.mephi.db.application.adapter.io.InputBoundary;
import ru.mephi.db.application.adapter.io.OutputBoundary;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.DatabaseInitException;
import ru.mephi.db.exception.DatabaseQuitException;
import ru.mephi.db.infrastructure.Constants;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@AllArgsConstructor(onConstructor_ = @Inject)
public class InitializeDatabaseUseCase {

    private final InputBoundary input;
    private final OutputBoundary output;
    private final CreateDatabaseUseCase createDatabaseUseCase;

    public FileLock execute(Path dbPath) throws DatabaseException {
        if (!Files.exists(dbPath)) {
            boolean create = IOUtils.promptYesNo(
                    input,
                    output,
                    "Database directory does not exist!\nDo you want to create a new database (y/N): ",
                    true
            );

            if (!create)
                throw new DatabaseQuitException("Database creation was aborted");

            createDatabaseUseCase.execute(dbPath);
        }

        if (!Files.isDirectory(dbPath))
            throw new DatabaseInitException("Provided path is not a directory");

        Path dbInfoFile = dbPath.resolve(Constants.DB_INFO_FILE);
        if (!Files.exists(dbInfoFile))
            throw new DatabaseInitException("Database metadata file missing");

        Path dbLogFile = dbPath.resolve(Constants.DB_LOG_FILE);
        if (!Files.exists(dbLogFile))
            throw new DatabaseInitException("Database log file missing");

        try {
            Path lockFile = dbPath.resolve(Constants.DB_LOCK_FILE);
            @SuppressWarnings("resource")
            FileChannel lockChannel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock lock = lockChannel.tryLock();

            if (lock == null)
                throw new DatabaseInitException("Database is already in use");

            return lock;
        } catch (IOException e) {
            throw new DatabaseInitException("Failed to acquire database lock");
        }
    }
}
