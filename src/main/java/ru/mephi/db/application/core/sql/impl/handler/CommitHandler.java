package ru.mephi.db.application.core.sql.impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.application.core.TransactionManager;

public class CommitHandler implements QueryHandler {
    private final TransactionManager transactionManager;

    public CommitHandler(TransactionManager transactionManager, DataRepository dataRepository, ConnectionConfig connectionConfig) {
        this.transactionManager = transactionManager;
    }

    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.COMMIT;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            if(!transactionManager.isInTransaction()) {
                return new QueryResult(false, null, "No transaction to commit");
            }

            transactionManager.commit();
            
            return new QueryResult(true, null, "Transaction committed successfully");
        } catch (Exception e) {
            return new QueryResult(false, null, "Failed to commit transaction: " + e.getMessage());
        }
    }
}