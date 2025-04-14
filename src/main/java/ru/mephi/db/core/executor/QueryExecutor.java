package ru.mephi.db.core.executor;

import ru.mephi.db.exception.QueryExecutionException;
import ru.mephi.db.model.query.Query;
import ru.mephi.db.model.query.QueryResult;

public interface QueryExecutor {
    QueryResult execute(Query query) throws QueryExecutionException;
}
