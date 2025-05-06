package ru.mephi.db;

import org.junit.Test;
import ru.mephi.db.exception.DatabaseQuitException;


import static org.junit.Assert.assertTrue;

public class ExitCommandIntegrationTest extends BaseIntegration {

    @Test
    public void testExitCommand() throws Exception {
        // Arrange
        component.getTestModule().addToInputList("exit");

        // Act & Assert
        try {
            component.getHandleUserInputUseCase().execute();
        } catch (Exception e) {
            assertTrue(e instanceof DatabaseQuitException);
            assertTrue(component.getTestModule().getOutputText().contains("Goodbye, dear!"));
        }
    }

    @Test
    public void testExitWithAlias() throws Exception {
        // Arrange
        component.getTestModule().addToInputList(":q");

        // Act & Assert
        try {
            component.getHandleUserInputUseCase().execute();
        } catch (Exception e) {
            assertTrue(e instanceof DatabaseQuitException);
            assertTrue(component.getTestModule().getOutputText().contains("Goodbye, dear!"));
        }
    }

    @Test
    public void testExitWithAnotherAlias() throws Exception {
        // Arrange
        component.getTestModule().addToInputList("quit");

        // Act & Assert
        try {
            component.getHandleUserInputUseCase().execute();
        } catch (Exception e) {
            assertTrue(e instanceof DatabaseQuitException);
            assertTrue(component.getTestModule().getOutputText().contains("Goodbye, dear!"));
        }
    }
}