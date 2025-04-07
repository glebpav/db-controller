package ru.mephi.db.usecase;

import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.DatabaseInitException;
import ru.mephi.db.exception.DatabaseQuitException;
import ru.mephi.db.util.Constants;
import ru.mephi.db.util.console.ScannerUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class InitializeDatabaseUseCase {

    private final Scanner scanner;

    private final CreateDatabaseUseCase createDatabaseUseCase;

    @Inject
    public InitializeDatabaseUseCase(
            Scanner scanner,
            CreateDatabaseUseCase createDatabaseUseCase
    ) {
        this.scanner = scanner;
        this.createDatabaseUseCase = createDatabaseUseCase;
    }

    public FileLock execute(Path dbPath) throws DatabaseException {
        if (!Files.exists(dbPath)) {
            boolean create = ScannerUtils.promptYesNo(
                    scanner,
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
