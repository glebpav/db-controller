package ru.mephi.db.application.core.sql;

import ru.mephi.db.exception.QueryExecutionException;
import ru.mephi.db.domain.entity.query.Query;
import ru.mephi.db.domain.entity.QueryResult;

public interface QueryExecutor {
    QueryResult execute(Query query) throws QueryExecutionException;
}
