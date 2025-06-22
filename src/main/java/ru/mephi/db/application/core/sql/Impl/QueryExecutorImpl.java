package ru.mephi.db.application.core.sql.Impl;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.core.sql.QueryExecutor;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.exception.QueryExecutionException;
import ru.mephi.db.domain.entity.query.Query;
import ru.mephi.db.domain.entity.QueryResult;

import java.util.List;


@AllArgsConstructor
public class QueryExecutorImpl implements QueryExecutor {
    private final List<QueryHandler> handlers;

    @Override
    public QueryResult execute(Query query) throws QueryExecutionException {
        QueryHandler handler = handlers.stream()
                .filter(h -> h.canHandle(query.getType()))
                .findFirst()
                .orElseThrow(() -> new QueryExecutionException(
                        "No handler found for query type: " + query.getType())
                );

        return handler.handle(query);
    }
}
