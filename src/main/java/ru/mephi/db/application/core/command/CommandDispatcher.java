package ru.mephi.db.application.core.command;

import ru.mephi.db.exception.DatabaseException;

public interface CommandDispatcher {
    void handle(String input) throws DatabaseException;
}
