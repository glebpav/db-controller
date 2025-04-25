package unit.ru.mephi.db.usecase;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.mephi.db.exception.DatabaseCreateException;
import ru.mephi.db.application.usecase.CreateDatabaseUseCase;
import ru.mephi.db.infrastructure.Constants;
import ru.mephi.db.infrastructure.cli.CliOutputBoundaryImpl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class CreateDataBaseUseCaseTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void shouldCreateDatabaseSuccessfully() throws Exception {
        // Arrange
        Path testPath = tempFolder.newFolder("test_db").toPath();
        CreateDatabaseUseCase useCase = new CreateDatabaseUseCase(new CliOutputBoundaryImpl());

        // Act
        useCase.execute(testPath);

        // Assert
        assertTrue(Files.exists(testPath));
        assertTrue(Files.exists(testPath.resolve(Constants.DB_INFO_FILE)));
        assertTrue(Files
                .readAllLines(testPath.resolve(Constants.DB_INFO_FILE)).get(0)
                .startsWith(Constants.MAGIC_HEADER)
        );
    }

    @Test(expected = DatabaseCreateException.class)
    public void shouldThrowExceptionWhenDirectoryCreationFails() throws Exception {
        // Skip test on Windows
        Assume.assumeFalse("Test skipped on Windows", System.getProperty("os.name").toLowerCase().contains("win"));

        // Arrange
        Path invalidPath = Path.of("/invalid/path");
        CreateDatabaseUseCase useCase = new CreateDatabaseUseCase(new CliOutputBoundaryImpl());

        // Act & Assert
        useCase.execute(invalidPath);
    }

    @Test
    public void shouldWriteCorrectFileContent() throws Exception {
        // Arrange
        Path testPath = tempFolder.newFolder("content_test").toPath();
        CreateDatabaseUseCase useCase = new CreateDatabaseUseCase(new CliOutputBoundaryImpl());

        // Act
        useCase.execute(testPath);

        // Assert
        Path infoFile = testPath.resolve(Constants.DB_INFO_FILE);
        String content = Files.readString(infoFile);

        assertTrue(content.contains("version=1.0.0"));
        assertTrue(content.contains("createdAt="));
        assertTrue(content.startsWith(Constants.MAGIC_HEADER));
    }

    @Test
    public void shouldPrintSuccessMessage() throws Exception {
        // Arrange
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        Path testPath = tempFolder.newFolder("test_db").toPath();
        CreateDatabaseUseCase useCase = new CreateDatabaseUseCase(new CliOutputBoundaryImpl());

        // Act
        useCase.execute(testPath);

        // Assert
        assertTrue(outContent.toString().contains("Database created successfully"));
        System.setOut(System.out); // Вернуть оригинальный System.out
    }

}
