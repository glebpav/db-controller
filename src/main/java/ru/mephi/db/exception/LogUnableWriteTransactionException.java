package ru.mephi.db.exception;

import java.io.IOException;

public class LogUnableWriteTransactionException extends DatabaseException {
    public LogUnableWriteTransactionException(String message, IOException e) {
        super(message);
    }
}
