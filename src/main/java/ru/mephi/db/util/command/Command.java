package ru.mephi.db.util.command;

import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.model.command.UserInputCommand;
import ru.mephi.db.model.command.UserInputCommandType;

public interface Command {
    boolean canHandle(UserInputCommandType userInputCommandType);
    void execute(String commandText) throws DatabaseException;
}
