package ru.mephi.db.core.executor;

import lombok.AllArgsConstructor;
import ru.mephi.db.core.handler.QueryHandler;
import ru.mephi.db.exception.QueryExecutionException;
import ru.mephi.db.model.query.Query;
import ru.mephi.db.model.query.QueryResult;

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
