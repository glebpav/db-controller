package ru.mephi.db;

import ru.mephi.db.di.AppModule;
import ru.mephi.db.di.DaggerMainComponent;
import ru.mephi.db.di.MainComponent;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.DatabaseInitException;
import ru.mephi.db.exception.DatabaseQuitException;
import ru.mephi.db.application.usecase.HandleUserInputUseCase;

import java.io.File;
import java.nio.channels.FileLock;
import java.nio.file.Path;

public class Main {
    private static MainComponent component;

    private static FileLock lock;

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                throw new DatabaseInitException("Received invalid arguments");
            }

            component = DaggerMainComponent.builder()
                    .appModule(new AppModule(Path.of(args[0])))
                    .build();

            lock = initializeDatabase(args);

            HandleUserInputUseCase handler = component.getHandleUserInputUseCase();
            handler.execute();
        } catch (DatabaseQuitException e) {
            // TODO: remove usage of pure sout
            System.out.println("\n" + e.getMessage());
        } catch (DatabaseException e) { // TODO: Handle properly
            e.printStackTrace(); // TODO: Logger
        } catch(Throwable th) {
            th.printStackTrace();
        } finally {
            exitDatabase();
        }
    }

    private static FileLock initializeDatabase(String[] args) throws DatabaseException {
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
