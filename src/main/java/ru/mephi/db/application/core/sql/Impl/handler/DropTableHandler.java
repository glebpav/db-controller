package ru.mephi.db.application.core.sql.Impl.handler;

import lombok.RequiredArgsConstructor;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.infrastructure.db.DataRepositoryImpl;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DropTableHandler implements QueryHandler {
    private final DataRepository dataRepository;
    private final ConnectionConfig connectionconfig ;

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

            String dbFilePath = connectionconfig.getDbPath();
            String tableFilePath = dbFilePath + "\\" + tableName + ".txt";

           dataRepository.deleteTableFile(tableFilePath);

            return new QueryResult(
                    true,
                    null,
                    true ? "Table '" + tableName + "' dropped successfully"
                            : "Failed to drop table '" + tableName + "'"
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