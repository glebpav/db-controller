package ru.mephi.db.application.core.sql;

import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

public interface QueryHandler {
    boolean canHandle(QueryType type);
    QueryResult handle(Query query);
}
