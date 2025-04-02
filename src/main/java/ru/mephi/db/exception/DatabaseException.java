package ru.mephi.db.exception;

public abstract class DatabaseException extends Exception {
    public DatabaseException(String message) {
        super(message);
    }
}
