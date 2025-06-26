// BaseIntegrationTest.java
package ru.mephi.db;

import ru.mephi.db.di.AppModule;
import ru.mephi.db.di.DaggerTestComponent;
import ru.mephi.db.di.TestComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import ru.mephi.db.infrastructure.Constants;

import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseIntegration {

    protected TestComponent component;
    protected FileLock lock;
    protected Path tempDbPath;

    @BeforeEach
    public void setUp() throws Exception {
        tempDbPath = Files.createTempDirectory("test_db");
        Files.createFile(tempDbPath.resolve(Constants.DB_INFO_FILE));
        Files.createFile(tempDbPath.resolve(Constants.DB_LOG_FILE));

        component = DaggerTestComponent.builder()
                .appModule(new AppModule(tempDbPath))
                .build();

        lock = component.getInitializeDatabaseUseCase().execute(tempDbPath);
    }

    @AfterEach
    public void tearDown() throws Exception {
        component.getTestModule().clearIO();
        component.getTestModule().clearIO();

        if (lock != null) {
            component.getExitDatabaseUseCase().execute(lock);
        }
        deleteTempDirectory(tempDbPath);
    }

    private void deleteTempDirectory(Path path) throws Exception {
        Files.walk(path)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (Exception ignored) {}
                });
    }
}