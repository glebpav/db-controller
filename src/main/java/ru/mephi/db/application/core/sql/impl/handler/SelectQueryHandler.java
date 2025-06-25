package ru.mephi.db.application.core.sql.impl.handler;

import lombok.RequiredArgsConstructor;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.ConnectionConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class SelectQueryHandler implements QueryHandler {
    private final DataRepository dataRepository;
    private final ConnectionConfig connectionconfig ;


    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.SELECT;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            String tableName = query.getTable();
            String dbFilePath = connectionconfig.getDbPath();
            String tableFilePath = dbFilePath + "\\" + tableName + ".txt";
            List<Map<String, Object>> resultData;

            if (query.getWhereClause() != null) {
                List<Integer> matchingIndices = findMatchingIndices(query);
                resultData = getRecordsByIndices(tableFilePath, matchingIndices, query.getColumnIndices());
            } else {
                List<Integer> allIndices = dataRepository.getAllRecordIndices(tableFilePath);
                resultData = getRecordsByIndices(tableFilePath, allIndices, query.getColumnIndices());
            }

            return QueryResult.builder()
                    .success(true)
                    .rows(resultData)
                    .message(String.format("Selected %d rows from %s", resultData.size(), query.getTable()))
                    .build();
        } catch (Exception e) {
            return QueryResult.builder()
                    .success(false)
                    .rows(List.of())
                    .message("SELECT failed: " + e.getMessage())
                    .build();
        }
    }

    private List<Integer> findMatchingIndices(Query query) throws IOException {
        String tableName = query.getTable();
        String whereClause = query.getWhereClause();
        String dbFilePath = connectionconfig.getDbPath();
        String tableFilePath = dbFilePath + "\\" + tableName + ".txt";

        if (whereClause == null || whereClause.trim().isEmpty()) {
            return dataRepository.getAllRecordIndices(tableFilePath);
        }

        whereClause = whereClause.replaceAll("\\s*(=|!=|<=|>=|<|>|LIKE)\\s*", " $1 ").trim();

        if (whereClause.toUpperCase().contains(" LIKE ")) {
            return handleLikeCondition(whereClause, tableFilePath);
        }

        return handleComparisonCondition(whereClause, tableFilePath);
    }

    private List<Integer> handleLikeCondition(String condition, String tablePath) throws IOException {
        String[] parts = condition.split("(?i) LIKE ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid LIKE condition format. Expected: 'column LIKE pattern'");
        }

        parts[0] = parts[0].replaceAll("['\"]", "").trim();
        parts[0] = parts[0].replaceAll("col", "").trim();
        int columnIndex = parseColumnIndex(parts[0]);
        String pattern = parts[1].replaceAll("['\"]", "").trim();
        boolean caseSensitive = !pattern.equals(pattern.toLowerCase());

        return dataRepository.findRecordsByPattern(tablePath, columnIndex, pattern, caseSensitive);
    }

    private List<Integer> handleComparisonCondition(String condition, String tablePath) throws IOException {
        String operator = extractOperator(condition);
        String[] parts = condition.split(operator, 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid condition format. Expected: 'column operator value'");
        }

        if (operator.equals("=")) {
            operator = "==";
        }

        parts[0] = parts[0].replaceAll("['\"]", "").trim();
        parts[0] = parts[0].replaceAll("col", "").trim();
        int columnIndex = parseColumnIndex(parts[0]);

        if (containColIndex(parts[1])){
            parts[1] = parts[1].replaceAll("['\"]", "").trim();
            parts[1] = parts[1].replaceAll("col", "").trim();
            int columnIndex2 = parseColumnIndex(parts[1]);
            return dataRepository.findRecordsByCondition(tablePath, columnIndex, operator, columnIndex2);
        }
        else {
            String value = parts[1].replaceAll("['\"]", "").trim();
            return dataRepository.findRecordsByConstant(tablePath, columnIndex, operator, value);
        }
    }

    private boolean containColIndex(String str) {
        try {
            return str.contains("col");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Column index must be a number: " + str);
        }
    }

    // Вспомогательные методы
    private int parseColumnIndex(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Column index must be a number: " + str);
        }
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+");
    }

    private String extractOperator(String condition) {
        if (condition.contains("!=")) return "!=";
        if (condition.contains("<=")) return "<=";
        if (condition.contains(">=")) return ">=";
        if (condition.contains("=")) return "=";
        if (condition.contains(">")) return ">";
        if (condition.contains("<")) return "<";
        throw new IllegalArgumentException("Unsupported operator in condition: " + condition);
    }


    private List<Map<String, Object>> getRecordsByIndices(
            String tablePath,
            List<Integer> indices,
            List<Integer> columnIndices
    ) throws IOException {
        List<Map<String, Object>> result = new ArrayList<>();

        for (int index : indices) {
            List<Object> record = dataRepository.readRecord(tablePath, index, 0); // Предполагаем, что 0 — это смещение
            Map<String, Object> row = new HashMap<>();

            if (columnIndices == null || columnIndices.isEmpty()) {
                for (int i = 0; i < record.size(); i++) {
                    row.put(String.valueOf(i), record.get(i));
                }
            } else {
                for (int colIndex : columnIndices) {
                    if (colIndex < record.size()) {
                        row.put(String.valueOf(colIndex), record.get(colIndex));
                    }
                }
            }
            result.add(row);
        }
        return result;
    }
}