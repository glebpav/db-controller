package ru.mephi.db.core.handler;

import ru.mephi.db.model.query.Query;
import ru.mephi.db.model.query.QueryResult;
import ru.mephi.db.model.query.QueryType;

public interface QueryHandler {
    boolean canHandle(QueryType type);
    QueryResult handle(Query query);
}
