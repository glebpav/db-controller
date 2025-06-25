package ru.mephi.db;

import org.junit.jupiter.api.Test;
import ru.mephi.db.exception.DatabaseQuitException;

import static org.junit.jupiter.api.Assertions.*;

public class HelpCommandIntegrationTest extends BaseIntegration {

    @Test
    public void testHelpCommand() {
        // Arrange
        component.getTestModule().addToInputList("help");
        component.getTestModule().addToInputList("exit");

        // Act & Assert
        assertThrows(DatabaseQuitException.class, () -> {
            component.getHandleUserInputUseCase().execute();
        });
        String output = component.getTestModule().getOutputText();
        assertTrue(output.contains("There is no help =("));
        assertTrue(output.contains("Goodbye, dear!"));
    }

    @Test
    public void testHelpWithAlias() {
        // Arrange
        component.getTestModule().addToInputList(":h");
        component.getTestModule().addToInputList("exit");

        // Act & Assert
        assertThrows(DatabaseQuitException.class, () -> {
            component.getHandleUserInputUseCase().execute();
        });
        String output = component.getTestModule().getOutputText();
        assertTrue(output.contains("There is no help =("));
        assertTrue(output.contains("Goodbye, dear!"));
    }
}