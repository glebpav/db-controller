package ru.mephi.db.util.command;

import lombok.AllArgsConstructor;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.DatabaseExitException;
import ru.mephi.db.exception.DatabaseQuitException;
import ru.mephi.db.model.command.UserInputCommand;
import ru.mephi.db.model.command.UserInputCommandType;
import ru.mephi.db.usecase.ExitDatabaseUseCase;
import ru.mephi.db.util.Constants;
import ru.mephi.db.util.io.OutputUtils;

import javax.inject.Inject;


@AllArgsConstructor(onConstructor_ = @Inject)
public class ExitCommand implements Command {
    OutputUtils output;

    @Override
    public boolean canHandle(UserInputCommandType userInputCommandType) {
        return userInputCommandType == UserInputCommandType.EXIT_COMMAND;
    }

    @Override
    public void execute(String commandText) throws DatabaseException {
        output.info(Constants.EXIT_MESSAGE);
        throw new DatabaseQuitException("Quit command exited");
    }
}
