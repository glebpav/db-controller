package ru.mephi.db.application.core.command;

import ru.mephi.db.exception.DatabaseException;

public interface CommandHandler {
    boolean canHandle(String commandText);
    void execute(String commandText) throws DatabaseException;
}
