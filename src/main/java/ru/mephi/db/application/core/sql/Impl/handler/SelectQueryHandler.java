package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectQueryHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.SELECT;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            List<Map<String, Object>> allData = getMockData(query.getTable());
            List<Map<String, Object>> filteredData = filterData(allData, query);
            List<Map<String, Object>> resultData = selectColumns(filteredData, query);

            return QueryResult.builder()
                    .success(true)
                    .rows(resultData)
                    .message(String.format("Selected %d rows from %s",
                            resultData.size(), query.getTable()))
                    .build();
        } catch (Exception e) {
            return QueryResult.builder()
                    .success(false)
                    .rows(List.of())
                    .message("SELECT failed: " + e.getMessage())
                    .build();
        }
    }

    private List<Map<String, Object>> getMockData(String tableName) {
        List<Map<String, Object>> data = new ArrayList<>();

        if ("products".equals(tableName)) {
            Map<String, Object> row1 = new HashMap<>();
            row1.put("0", 1);       // ID
            row1.put("1", "Apple"); // Название
            row1.put("2", 10.5);    // Цена
            data.add(row1);

            Map<String, Object> row2 = new HashMap<>();
            row2.put("0", 2);
            row2.put("1", "Banana");
            row2.put("2", 5.3);
            data.add(row2);
        }
        return data;
    }

    private List<Map<String, Object>> filterData(List<Map<String, Object>> data, Query query) {
        if (query.getWhereClause() == null) {
            return data;
        }

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> row : data) {
            if (matchesWhereCondition(row, query.getWhereClause())) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private boolean matchesWhereCondition(Map<String, Object> row, String whereClause) {
        // Упрощенная проверка условий
        if (whereClause.contains("LIKE")) {
            return handleLikeCondition(row, whereClause);
        } else if (whereClause.contains("=")) {
            return handleEqualsCondition(row, whereClause);
        } else if (whereClause.contains(">")) {
            return handleGreaterThanCondition(row, whereClause);
        } else if (whereClause.contains("<")) {
            return handleLessThanCondition(row, whereClause);
        }
        return false;
    }

    private boolean handleLikeCondition(Map<String, Object> row, String condition) {
        String[] parts = condition.split("LIKE");
        int columnIndex = Integer.parseInt(parts[0].trim());
        String pattern = parts[1].trim().replaceAll("'", "");

        String value = row.get(String.valueOf(columnIndex)).toString();
        pattern = pattern.replace("%", ".*").replace("_", ".");

        return value.matches(pattern);
    }

    private boolean handleEqualsCondition(Map<String, Object> row, String condition) {
        String[] parts = condition.split("=");
        int columnIndex = Integer.parseInt(parts[0].trim());
        String expectedValue = parts[1].trim().replaceAll("'", "");

        Object actualValue = row.get(String.valueOf(columnIndex));
        return actualValue != null && actualValue.toString().equals(expectedValue);
    }

    private boolean handleGreaterThanCondition(Map<String, Object> row, String condition) {
        String[] parts = condition.split(">");
        int columnIndex = Integer.parseInt(parts[0].trim());
        String valueStr = parts[1].trim().replaceAll("'", "");

        Object value = row.get(String.valueOf(columnIndex));
        if (value instanceof Number && isNumeric(valueStr)) {
            double rowValue = ((Number) value).doubleValue();
            double conditionValue = Double.parseDouble(valueStr);
            return rowValue > conditionValue;
        }
        return false;
    }

    private boolean handleLessThanCondition(Map<String, Object> row, String condition) {
        String[] parts = condition.split("<");
        int columnIndex = Integer.parseInt(parts[0].trim());
        String valueStr = parts[1].trim().replaceAll("'", "");

        Object value = row.get(String.valueOf(columnIndex));
        if (value instanceof Number && isNumeric(valueStr)) {
            double rowValue = ((Number) value).doubleValue();
            double conditionValue = Double.parseDouble(valueStr);
            return rowValue < conditionValue;
        }
        return false;
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private List<Map<String, Object>> selectColumns(
            List<Map<String, Object>> data,
            Query query) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map<String, Object> row : data) {
            Map<String, Object> selectedRow = new HashMap<>();

            if (query.getColumnIndices() == null || query.getColumnIndices().isEmpty()) {
                selectedRow.putAll(row);
            } else {
                for (Integer colIndex : query.getColumnIndices()) {
                    String key = String.valueOf(colIndex);
                    if (row.containsKey(key)) {
                        selectedRow.put(key, row.get(key));
                    }
                }
            }

            result.add(selectedRow);
        }
        return result;
    }
}