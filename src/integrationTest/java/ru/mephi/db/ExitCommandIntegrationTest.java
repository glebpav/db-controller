package ru.mephi.db;

import org.junit.jupiter.api.Test;
import ru.mephi.db.exception.DatabaseQuitException;

import static org.junit.jupiter.api.Assertions.*;

public class ExitCommandIntegrationTest extends BaseIntegration {

    @Test
    public void testExitCommand() {
        // Arrange
        component.getTestModule().addToInputList("exit");

        // Act & Assert
        Exception e = assertThrows(DatabaseQuitException.class, () -> {
            component.getHandleUserInputUseCase().execute();
        });
        assertTrue(component.getTestModule().getOutputText().contains("Goodbye, dear!"));
    }

    @Test
    public void testExitWithAlias() {
        // Arrange
        component.getTestModule().addToInputList(":q");

        // Act & Assert
        Exception e = assertThrows(DatabaseQuitException.class, () -> {
            component.getHandleUserInputUseCase().execute();
        });
        assertTrue(component.getTestModule().getOutputText().contains("Goodbye, dear!"));
    }

    @Test
    public void testExitWithAnotherAlias() {
        // Arrange
        component.getTestModule().addToInputList("quit");

        // Act & Assert
        Exception e = assertThrows(DatabaseQuitException.class, () -> {
            component.getHandleUserInputUseCase().execute();
        });
        assertTrue(component.getTestModule().getOutputText().contains("Goodbye, dear!"));
    }
}