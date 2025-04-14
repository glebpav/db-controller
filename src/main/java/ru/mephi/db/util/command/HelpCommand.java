package ru.mephi.db.util.command;

import lombok.AllArgsConstructor;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.model.command.UserInputCommand;
import ru.mephi.db.model.command.UserInputCommandType;
import ru.mephi.db.util.Constants;
import ru.mephi.db.util.io.OutputUtils;

@AllArgsConstructor
public class HelpCommand implements Command{
    OutputUtils output;

    @Override
    public boolean canHandle(UserInputCommandType userInputCommandType) {
        return userInputCommandType == UserInputCommandType.HELP_COMMAND;
    }

    @Override
    public void execute(String commandText) throws DatabaseException {
        output.info(Constants.HELP_MESSAGE);
    }
}
