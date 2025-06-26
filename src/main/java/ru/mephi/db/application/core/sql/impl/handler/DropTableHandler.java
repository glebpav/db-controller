package ru.mephi.db.application.core.sql.impl.handler;

import lombok.RequiredArgsConstructor;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.TransactionManager;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.exception.LogUnableWriteTransactionException;

@RequiredArgsConstructor
public class DropTableHandler implements QueryHandler {
    private final DataRepository dataRepository;
    private final TransactionManager transactionManager;

    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.DROP_TABLE;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            String tableName = query.getTable();

            if (tableName == null || tableName.isEmpty()) {
                return new QueryResult(
                        false,
                        null,
                        "Table name is required for DROP TABLE operation"
                );
            }

            String tableFilePath = transactionManager.getActualTablePath(tableName).toString();
    
            dataRepository.deleteTableFile(tableFilePath);

            try {
                transactionManager.logDropTable(tableName);
            } catch (LogUnableWriteTransactionException e) {
                System.err.println("Warning: Failed to log drop table operation: " + e.getMessage());
            }

            return new QueryResult(
                    true,
                    null,
                    "Table '" + tableName + "' dropped successfully"
            );
        } catch (Exception e) {
            return new QueryResult(
                    false,
                    null,
                    "Failed to drop table: " + e.getMessage()
            );
        }
    }
}