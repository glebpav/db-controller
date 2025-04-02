package ru.mephi.db.exception;

import java.io.IOException;

public class DatabaseCreateException extends DatabaseException {
    public DatabaseCreateException(IOException e) {
        super("Failed to create database: " + e.getMessage());
    }
}
