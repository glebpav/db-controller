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
import java.util.List;

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
            String dbFilePath = connectionConfig.getDbPath().resolve("Master.txt").toString();
            List<String> tableNames = dataRepository.getAllTableNames(dbFilePath);
            for (String tableName : tableNames) {
                Path mainPath = connectionConfig.getTablePath(tableName);
                Path tempPath = transactionManager.getTempTablePath(tableName);
                if (Files.exists(tempPath)) {
                    Files.move(tempPath, mainPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
            transactionManager.commit();
            return new QueryResult(true, null, "Transaction committed successfully");
        } catch (Exception e) {
            return new QueryResult(false, null, "Failed to commit transaction: " + e.getMessage());
        }
    }
}