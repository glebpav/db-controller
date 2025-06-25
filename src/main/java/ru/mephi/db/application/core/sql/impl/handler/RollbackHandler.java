package ru.mephi.db.application.core.sql.impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

public class RollbackHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.ROLLBACK;
    }

    @Override
    public QueryResult handle(Query query) {
        System.out.println("Rolling back transaction");
        // Логика отката транзакции
        return new QueryResult(true, null, "Transaction rolled back successfully");
    }
}