package ru.mephi.db.exception.parser;

import ru.mephi.db.exception.DatabaseException;

public class QueryParamsException extends SQLParseException {
    public QueryParamsException(String message) {
        super(message);
    }
    public QueryParamsException() {
        super("Query params error");
    }
}
