package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class InsertQueryHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.INSERT;
    }

    @Override
    public QueryResult handle(Query query) {
        System.out.println("Обработка INSERT-запроса:");
        System.out.println("Таблица: " + query.getTable());
        System.out.println("Колонки: " + query.getColumns());
        System.out.println("Значения: " + query.getData());

        try {
            Map<String, Object> valuesMap = query.getData();
            int insertedCount = 1;

            String message = String.format("Inserted into %s: %s values %s",
                    query.getTable(),
                    query.getColumns(),
                    valuesMap);

            return QueryResult.builder()
                    .success(true)
                    .rows(List.of(Map.of(
                            "inserted_rows", insertedCount,
                            "generated_keys", Collections.emptyList()
                    )))
                    .message(message)
                    .build();

        } catch (Exception e) {
            return QueryResult.builder()
                    .success(false)
                    .rows(Collections.emptyList())
                    .message("Failed to execute INSERT: " + e.getMessage())
                    .build();
        }
    }
}