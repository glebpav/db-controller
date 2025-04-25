package ru.mephi.db.application.core.sql;

import ru.mephi.db.exception.QueryExecutionException;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;

public interface QueryExecutor {
    QueryResult execute(Query query) throws QueryExecutionException;
}
