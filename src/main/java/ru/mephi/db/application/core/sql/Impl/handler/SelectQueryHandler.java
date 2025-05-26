package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class SelectQueryHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.SELECT;
    }

    @Override
    public QueryResult handle(Query query) {
        System.out.println("Обработка SELECT-запроса:");
        System.out.println("Таблица: " + query.getTable());
        System.out.println("Колонки: " + query.getColumns());
        if (query.getWhereClause() != null) {
            System.out.println("WHERE: " + query.getWhereClause());
        }

        // Пример бд заполненной не знаю как хранить будете
        try {
            List<Map<String, Object>> result = new ArrayList<>();

            Map<String, Object> row1 = new HashMap<>();
            row1.put("id", 1);
            row1.put("name", "Иван");
            result.add(row1);

            Map<String, Object> row2 = new HashMap<>();
            row2.put("id", 2);
            row2.put("name", "Мария");
            result.add(row2);

            return new QueryResult(true, result, null);
        } catch (Exception e) {
            return new QueryResult(false, null, "Ошибка при выполнении запроса: " + e.getMessage());
        }
    }
}