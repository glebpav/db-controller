// BeginTransactionHandler.java
package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

public class BeginTransactionHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.BEGIN_TRANSACTION;
    }

    @Override
    public QueryResult handle(Query query) {
        // Реальная логика начала транзакции
        return new QueryResult(true, null, "Transaction started");
    }
}