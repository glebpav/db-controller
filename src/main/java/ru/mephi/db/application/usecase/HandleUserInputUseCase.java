package ru.mephi.db.application.usecase;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.adapter.cli.InputBoundary;
import ru.mephi.db.application.adapter.cli.OutputBoundary;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.DatabaseQuitException;
import ru.mephi.db.application.core.command.impl.CommandDispatcherImpl;

@AllArgsConstructor
public class HandleUserInputUseCase {
    OutputBoundary outputBoundary;
    InputBoundary inputBoundary;
    CommandDispatcherImpl commandDispatcher;

    public void execute() throws DatabaseException {
        System.out.print("> ");
        String userInput = inputBoundary.next().trim();

        try {
            commandDispatcher.handle(userInput);
        } catch (DatabaseQuitException e) {
            throw e;
        } catch (DatabaseException e) {
            outputBoundary.send(e.getMessage(), OutputBoundary.LogLevel.ERROR);
        }
    }
}
