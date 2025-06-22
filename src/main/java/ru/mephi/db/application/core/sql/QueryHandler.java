package ru.mephi.db.application.core.sql;

import ru.mephi.db.domain.entity.query.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.exception.QueryExecutionException;
import ru.mephi.db.exception.parser.SQLParseException;

public interface QueryHandler {
    boolean canHandle(QueryType type);
    QueryResult handle(Query query) throws QueryExecutionException;
}
