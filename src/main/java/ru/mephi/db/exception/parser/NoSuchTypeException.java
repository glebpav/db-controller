package ru.mephi.db.exception.parser;

import ru.mephi.db.exception.DatabaseException;

public class NoSuchTypeException extends DatabaseException {
    public NoSuchTypeException(String message) {
        super(message);
    }
}
