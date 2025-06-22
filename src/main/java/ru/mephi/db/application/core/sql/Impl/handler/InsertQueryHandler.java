package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

import java.util.List;
import java.util.Map;

public class InsertQueryHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.INSERT;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            List<Object> values = query.getValues();
            String tableName = query.getTable();

            // Здесь должна быть реальная логика вставки в таблицу
            System.out.printf("Inserting into %s: %s%n", tableName, values);

            return QueryResult.builder()
                    .success(true)
                    .message(String.format("Inserted 1 row into %s", tableName))
                    .rows(List.of(Map.of(
                            "inserted_rows", 1,
                            "values", values
                    )))
                    .build();
        } catch (Exception e) {
            return QueryResult.builder()
                    .success(false)
                    .message("Failed to execute INSERT: " + e.getMessage())
                    .rows(List.of())
                    .build();
        }
    }
}