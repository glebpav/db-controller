// BaseIntegrationTest.java
package integration;

import integration.di.DaggerTestComponent;
import integration.di.TestComponent;
import org.junit.After;
import org.junit.Before;

import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseIntegration {

    protected TestComponent component;
    protected FileLock lock;
    protected Path tempDbPath;

    @Before
    public void setUp() throws Exception {
        tempDbPath = Files.createTempDirectory("test_db");
        Files.createFile(tempDbPath.resolve("info"));

        component = DaggerTestComponent.create();
        lock = component.getInitializeDatabaseUseCase().execute(tempDbPath);
    }

    @After
    public void tearDown() throws Exception {
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