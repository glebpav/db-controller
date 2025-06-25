package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.application.core.TransactionManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CommitHandler implements QueryHandler {
    private final TransactionManager transactionManager;
    private final DataRepository dataRepository;
    private final ConnectionConfig connectionConfig;

    public CommitHandler(TransactionManager transactionManager, DataRepository dataRepository, ConnectionConfig connectionConfig) {
        this.transactionManager = transactionManager;
        this.dataRepository = dataRepository;
        this.connectionConfig = connectionConfig;
    }

    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.COMMIT;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            Boolean isInTransaction = transactionManager.isInTransaction();
            if(!isInTransaction) {
                return new QueryResult(false, null, "No transaction to commit");
            }

            for (String tableName : transactionManager.getTempTables()) {
                Path tempTableFilePath = transactionManager.getTempTablePath(tableName).toAbsolutePath();
                Path mainTablePath = connectionConfig.getTablePath(tableName).toAbsolutePath();
                Path masterPath = connectionConfig.getMasterPath().toAbsolutePath();
                if (Files.exists(tempTableFilePath)) {
                    Files.copy(tempTableFilePath, mainTablePath, StandardCopyOption.REPLACE_EXISTING);
                }
                dataRepository.deleteTableFile(tempTableFilePath.toString());

                if(!dataRepository.isTableExists(connectionConfig.getMasterPath().toAbsolutePath().toString(), mainTablePath.toString())) {
                    dataRepository.addTableReference(masterPath.toString(), mainTablePath.toString());
                }
            }

            transactionManager.commit();
            
            return new QueryResult(true, null, "Transaction committed successfully");
        } catch (Exception e) {
            return new QueryResult(false, null, "Failed to commit transaction: " + e.getMessage());
        }
    }
}