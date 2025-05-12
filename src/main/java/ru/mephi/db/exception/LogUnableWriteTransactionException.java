package ru.mephi.db.exception;

public class LogUnableWriteTransactionException extends DatabaseException {
    public LogUnableWriteTransactionException(String message) {
        super(message);
    }
}
