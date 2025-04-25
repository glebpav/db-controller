package ru.mephi.db.application.core.command.impl.handler;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.adapter.io.OutputBoundary;
import ru.mephi.db.application.core.command.CommandHandler;
import ru.mephi.db.application.core.command.CommandParserUtils;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.DatabaseQuitException;
import ru.mephi.db.infrastructure.Constants;

import javax.inject.Inject;


@AllArgsConstructor(onConstructor_ = @Inject)
public class ExitCommandHandler implements CommandHandler {
    OutputBoundary output;

    @Override
    public boolean canHandle(String input) {
        return CommandParserUtils.checkInSet(input, Constants.EXIT_COMMANDS);
    }

    @Override
    public void execute(String commandText) throws DatabaseException {
        output.info(Constants.EXIT_MESSAGE + "\n");
        throw new DatabaseQuitException("Quit command exited");
    }
}
