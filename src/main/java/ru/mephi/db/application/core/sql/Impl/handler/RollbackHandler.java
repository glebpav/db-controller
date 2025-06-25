package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.application.core.TransactionManager;

public class RollbackHandler implements QueryHandler {
    private final TransactionManager transactionManager;

    public RollbackHandler(TransactionManager transactionManager, DataRepository dataRepository, ConnectionConfig connectionConfig) {
        this.transactionManager = transactionManager;
    }

    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.ROLLBACK;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            if(!transactionManager.isInTransaction()) {
                return new QueryResult(false, null, "No transaction to rollback");
            }
            
            transactionManager.rollback();
            return new QueryResult(true, null, "Transaction rolled back successfully");
        } catch (Exception e) {
            return new QueryResult(false, null, "Failed to rollback transaction: " + e.getMessage());
        }
    }
}