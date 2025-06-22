package ru.mephi.db.exception.parser;

import ru.mephi.db.exception.DatabaseException;

public class SQLParseException extends DatabaseException {
    public SQLParseException(String message) {
        super(message);
    }
}
