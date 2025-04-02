package ru.mephi.db.exception;

import java.io.IOException;

public class DatabaseExitException extends DatabaseException {
    public DatabaseExitException(IOException e) {
        super("Failed to release lock or remove lock file: "+ e.getMessage());
    }
}
