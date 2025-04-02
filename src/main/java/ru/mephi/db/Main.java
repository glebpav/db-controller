package ru.mephi.db;

import ru.mephi.db.di.DaggerMainComponent;
import ru.mephi.db.di.MainComponent;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.DatabaseInitException;
import ru.mephi.db.exception.DatabaseQuitException;
import ru.mephi.db.usecase.HandleUserInputUseCase;

import java.io.File;
import java.nio.channels.FileLock;
import java.nio.file.Path;

public class Main {

    private static final MainComponent component = DaggerMainComponent.create();

    private static FileLock lock;

    public static void main(String[] args) {
        try {
            lock = initializeDatabase(args);

            HandleUserInputUseCase handler = component.getHandleUserInputUseCase();
            //noinspection InfiniteLoopStatement
            while (true) {
                handler.execute();
            }
        } catch (DatabaseQuitException e) {
            System.out.println("\n" + e.getMessage());
        } catch (DatabaseException e) { // TODO: Handle properly
            e.printStackTrace(); // TODO: Logger
        } finally {
            exitDatabase();
        }
    }

    private static FileLock initializeDatabase(String[] args) throws DatabaseException {
        if (args.length != 1)
            throw new DatabaseInitException("Received invalid arguments");

        File db = new File(args[0]);
        Path dbPath = db.toPath();
        return component.getInitializeDatabaseUseCase().execute(dbPath);
    }

    private static void exitDatabase() {
        try {
            if (lock != null)
                component.getExitDatabaseUseCase().execute(lock);
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }
}
