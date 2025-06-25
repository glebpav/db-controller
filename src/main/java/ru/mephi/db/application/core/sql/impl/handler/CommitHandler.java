package ru.mephi.db.application.core.sql.impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

public class CommitHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.COMMIT;
    }

    @Override
    public QueryResult handle(Query query) {
        System.out.println("Committing transaction");
        // Логика подтверждения транзакции
        return new QueryResult(true, null, "Transaction committed successfully");
    }
}