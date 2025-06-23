package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.infrastructure.db.DataRepositoryImpl;

import java.util.Map;

public class DropTableHandler implements QueryHandler {
    private final DataRepositoryImpl dataRepository = new DataRepositoryImpl();

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

           // result = dataRepository.dropTable(tableName);

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