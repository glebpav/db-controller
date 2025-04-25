package ru.mephi.db.application.usecase;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.adapter.io.InputBoundary;
import ru.mephi.db.application.adapter.io.OutputBoundary;
import ru.mephi.db.application.core.command.CommandDispatcher;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.DatabaseQuitException;

import javax.inject.Inject;

@AllArgsConstructor(onConstructor_ = @Inject)
public class HandleUserInputUseCase {
    private final OutputBoundary outputBoundary;
    private final InputBoundary inputBoundary;
    private final CommandDispatcher commandDispatcher;

    public void execute() throws DatabaseException {
        // TODO: add level for input prompt
        outputBoundary.info("> ");
        String userInput = inputBoundary.next().trim();

        try {
            commandDispatcher.handle(userInput);
        } catch (DatabaseQuitException e) {
            throw e;
        } catch (DatabaseException e) {
            outputBoundary.error(e.getMessage(), e);
        }
    }
}
