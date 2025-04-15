package ru.mephi.db.util.command;

import lombok.AllArgsConstructor;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.model.command.UserInputCommandType;
import ru.mephi.db.util.io.OutputUtils;

import javax.inject.Inject;

@AllArgsConstructor(onConstructor_ = @Inject)
public class EmptyCommand implements Command{
    OutputUtils outputUtils;

    @Override
    public boolean canHandle(UserInputCommandType userInputCommandType) {
        return userInputCommandType == UserInputCommandType.EMPTY_COMMAND;
    }

    @Override
    public void execute(String commandText) throws DatabaseException {

    }
}
