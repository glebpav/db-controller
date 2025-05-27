package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

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
            System.out.println("Полное условие WHERE: " + query.getWhereClause());
            // Здесь должна быть реальная фильтрация данных
        }


        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> row1 = new HashMap<>();
        row1.put("name", "Иван");
        row1.put("age", 30);
        row1.put("status", "active");
        result.add(row1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("name", "Мария");
        row2.put("age", 20);
        row2.put("status", "inactive");
        result.add(row2);

        // Реальная фильтрация
        if (query.getWhereClause() != null) {
            result = filterResults(result, query.getWhereClause());
        }

        return new QueryResult(true, result, null);
    }

    private List<Map<String, Object>> filterResults(List<Map<String, Object>> data, String whereClause) {
        //ПРИМЕР
        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> row : data) {
            boolean matches = true;

            if (whereClause.contains("age > 25")) {
                matches = matches && ((int) row.get("age") > 25);
            }
            if (whereClause.contains("status = 'active'")) {
                matches = matches && "active".equals(row.get("status"));
            }

            if (matches) {
                filtered.add(row);
            }
        }
        return filtered;
    }
}