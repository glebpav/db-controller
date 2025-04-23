package ru.mephi.db.application.core.command.impl.handler;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.core.command.CommandHandler;
import ru.mephi.db.exception.DatabaseException;

import javax.inject.Inject;

@AllArgsConstructor(onConstructor_ = @Inject)
public class EmptyCommandHandler implements CommandHandler {
    @Override
    public boolean canHandle(String input) {
        return input == null || input.isEmpty();
    }

    @Override
    public void execute(String commandText) throws DatabaseException {

    }
}
