package ru.mephi.db.application.core.command.impl.handler;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.adapter.io.OutputBoundary;
import ru.mephi.db.application.core.command.CommandHandler;
import ru.mephi.db.application.core.command.CommandParserUtils;
import ru.mephi.db.di.qulifier.CommandPriority;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.infrastructure.Constants;

import javax.inject.Inject;

@CommandPriority(0)
@AllArgsConstructor(onConstructor_ = @Inject)
public class HelpCommandHandler implements CommandHandler {
    OutputBoundary output;

    @Override
    public boolean canHandle(String input) {
        return CommandParserUtils.checkInSet(input, Constants.HELP_COMMANDS);
    }

    @Override
    public void execute(String commandText) throws DatabaseException {
        output.info(Constants.HELP_MESSAGE + "\n");
    }
}
