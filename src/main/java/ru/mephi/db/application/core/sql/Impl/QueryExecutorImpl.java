package ru.mephi.db.application.core.sql.Impl;

import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import ru.mephi.db.application.core.sql.QueryExecutor;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.exception.QueryExecutionException;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;

import java.util.List;

@AllArgsConstructor(onConstructor_ = @Inject)
public class QueryExecutorImpl implements QueryExecutor {
    private final List<QueryHandler> handlers; // Должен включать BeginTransactionHandler

    @SneakyThrows
    @Override
    public QueryResult execute(Query query) {
        return handlers.stream()
                .filter(h -> h.canHandle(query.getType()))
                .findFirst()
                .orElseThrow(() -> new QueryExecutionException("No handler for: " + query.getType()))
                .handle(query);
    }
}