package ru.mephi.db.util.command;

import lombok.AllArgsConstructor;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.UnsupportedCommandException;
import ru.mephi.db.model.command.UserInputCommand;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class CommandDispatcher {
    private final List<Command> commands;

    public void handle(UserInputCommand userInputCommand) throws DatabaseException {
        for (Command cmd : commands) {
            if (cmd.canHandle(userInputCommand.getUserInputType())) {
                cmd.execute(userInputCommand.getCommand());
                return;
            }
        }
        throw new UnsupportedCommandException("Unknown command: " + userInputCommand.getCommand());
    }
}
