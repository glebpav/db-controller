// HelpCommandIntegrationTest.java
package integration;

import org.junit.Test;
import ru.mephi.db.exception.DatabaseQuitException;

import static org.junit.Assert.assertTrue;

public class HelpCommandIntegrationTest extends BaseIntegration {

    @Test
    public void testHelpCommand() throws Exception {
        // Arrange
        component.getTestModule().addToInputList("help");
        component.getTestModule().addToInputList("exit");

        // Act & Assert
        try {
            component.getHandleUserInputUseCase().execute();
        } catch (Exception e) {
            assertTrue(e instanceof DatabaseQuitException);
            String output = component.getTestModule().getOutputText();
            assertTrue(output.contains("There is no help =("));
            assertTrue(output.contains("Goodbye, dear!"));
        }

    }

    @Test
    public void testHelpWithAlias() throws Exception {
        // Arrange
        component.getTestModule().addToInputList(":h");
        component.getTestModule().addToInputList("exit");

        // Act
        try {
            component.getHandleUserInputUseCase().execute();
        } catch (Exception e) {
            assertTrue(e instanceof DatabaseQuitException);
            String output = component.getTestModule().getOutputText();
            assertTrue(output.contains("There is no help =("));
            assertTrue(output.contains("Goodbye, dear!"));
        }
    }
}