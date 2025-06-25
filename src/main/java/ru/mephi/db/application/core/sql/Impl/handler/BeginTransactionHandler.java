// BeginTransactionHandler.java
package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.application.core.TransactionManager;
import java.util.List;

public class BeginTransactionHandler implements QueryHandler {
    private final DataRepository dataRepository;
    private final ConnectionConfig connectionConfig;
    private final TransactionManager transactionManager;

    public BeginTransactionHandler(TransactionManager transactionManager, DataRepository dataRepository, ConnectionConfig connectionConfig) {
        this.transactionManager = transactionManager;
        this.dataRepository = dataRepository;
        this.connectionConfig = connectionConfig;
    }

    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.BEGIN_TRANSACTION;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            transactionManager.begin();
            String dbFilePath = connectionConfig.getDbPath().resolve("Master.txt").toString();
            List<String> tableNames = dataRepository.getAllTableNames(dbFilePath);
            for (String tableName : tableNames) {
                transactionManager.createTempTableCopy(tableName);
            }
            return new QueryResult(true, null, "Transaction started");
        } catch (Exception e) {
            return new QueryResult(false, null, "Failed to start transaction: " + e.getMessage());
        }
    }
}