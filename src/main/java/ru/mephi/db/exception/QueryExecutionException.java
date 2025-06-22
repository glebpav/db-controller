package ru.mephi.db.exception;

import ru.mephi.db.exception.parser.QueryParamsException;

public class QueryExecutionException extends DatabaseException{

    public QueryExecutionException(String message) {
        super(message);
    }

    public QueryExecutionException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

}
