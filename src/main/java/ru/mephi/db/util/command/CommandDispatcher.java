package ru.mephi.db.util.command;

import lombok.AllArgsConstructor;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.UnsupportedCommandException;
import ru.mephi.db.model.command.UserInputCommand;

import java.util.List;

@AllArgsConstructor
public class CommandDispatcher {
    private final List<Command> commands;

    public void handle(UserInputCommand userInputCommand) throws DatabaseException {
        Command executor = commands.stream()
                .filter(cmd -> cmd.canHandle(userInputCommand.getUserInputType()))
                .findFirst()
                .orElseThrow(() -> new UnsupportedCommandException(
                        "Unknown command: " + userInputCommand.getCommand()
                ));

        executor.execute(userInputCommand.getCommand());
    }
}
