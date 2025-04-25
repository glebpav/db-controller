package ru.mephi.db.application.core.command.impl;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.core.command.CommandHandler;
import ru.mephi.db.application.core.command.CommandDispatcher;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.UnsupportedCommandException;

import javax.inject.Inject;
import java.util.List;

@AllArgsConstructor()
public class CommandDispatcherImpl implements CommandDispatcher {
    private final List<CommandHandler> commandHandlers;

    @Override
    public void handle(String input) throws DatabaseException {
        CommandHandler executor = commandHandlers.stream()
                .filter(cmd -> cmd.canHandle(input))
                .findFirst()
                .orElseThrow(() -> new UnsupportedCommandException(
                        "Unknown command: " + input
                ));

        executor.execute(input);
    }
}
