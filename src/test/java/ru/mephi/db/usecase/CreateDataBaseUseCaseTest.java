package ru.mephi.db.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import ru.mephi.db.exception.DatabaseCreateException;
import ru.mephi.db.application.usecase.CreateDatabaseUseCase;
import ru.mephi.db.infrastructure.Constants;
import ru.mephi.db.infrastructure.cli.CliOutputBoundaryImpl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CreateDataBaseUseCaseTest {

    @TempDir
    Path tempDir;

    @Test
    public void shouldCreateDatabaseSuccessfully() throws Exception {
        // Arrange
        Path testPath = tempDir.resolve("test_db");
        Files.createDirectory(testPath);
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

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void shouldThrowExceptionWhenDirectoryCreationFails() {
        // Arrange
        Path invalidPath = Path.of("/invalid/path");
        CreateDatabaseUseCase useCase = new CreateDatabaseUseCase(new CliOutputBoundaryImpl());

        // Act & Assert
        assertThrows(DatabaseCreateException.class, () -> useCase.execute(invalidPath));
    }

    @Test
    public void shouldWriteCorrectFileContent() throws Exception {
        // Arrange
        Path testPath = tempDir.resolve("content_test");
        Files.createDirectory(testPath);
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
        Path testPath = tempDir.resolve("test_db");
        Files.createDirectory(testPath);
        CreateDatabaseUseCase useCase = new CreateDatabaseUseCase(new CliOutputBoundaryImpl());

        try {
            // Act
            useCase.execute(testPath);

            // Assert
            assertTrue(outContent.toString().contains("Database created successfully"));
        } finally {
            System.setOut(System.out);
        }
    }
}