package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

import java.util.List;
import java.util.Map;

public class ShowTablesHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.SHOW_TABLES;
    }

    @Override
    public QueryResult handle(Query query) {
        try {

            List<Map<String, Object>> tables = getTablesList();

            return new QueryResult(true, tables, "Tables listed successfully");
        } catch (Exception e) {
            return new QueryResult(false, null, "Error showing tables: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> getTablesList() {
        // Заглушка - реализуйте реальную логику
        return List.of(
                Map.of("name", "users", "rows", 100),
                Map.of("name", "products", "rows", 500)
        );
    }
}