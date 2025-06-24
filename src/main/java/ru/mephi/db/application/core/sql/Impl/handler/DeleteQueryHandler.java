package ru.mephi.db.application.core.sql.Impl.handler;

import lombok.RequiredArgsConstructor;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.ConnectionConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DeleteQueryHandler implements QueryHandler {
    private final DataRepository dataRepository;
    private final ConnectionConfig connectionConfig;

    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.DELETE;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            String tableName = query.getTable();
            String dbFilePath = connectionConfig.getDbPath();
            String tableFilePath = dbFilePath + "\\" + tableName + ".txt";
            int deletedCount = 0;

            if (query.getWhereClause() != null) {
                List<Integer> matchingIndices = findMatchingIndices(query);
                for (int index : matchingIndices) {
                    dataRepository.deleteRecord(tableFilePath, index);
                }
            } else if (query.getRowIndex() != null) {
                dataRepository.deleteRecord(tableFilePath, query.getRowIndex()) ;
            } else {
                // Для очистки таблицы будем удалять записи по одной
                List<Integer> allIndices = dataRepository.getAllRecordIndices(tableFilePath);
                for (int index : allIndices) {
                    dataRepository.deleteRecord(tableFilePath, index);

                }
            }

            return QueryResult.builder()
                    .success(true)
                    .rows(List.of(Map.of("deleted", deletedCount)))
                    .message(String.format("Deleted %d rows from %s", deletedCount, query.getTable()))
                    .build();
        } catch (Exception e) {
            return QueryResult.builder()
                    .success(false)
                    .rows(List.of())
                    .message("DELETE failed: " + e.getMessage())
                    .build();
        }
    }

    private List<Integer> findMatchingIndices(Query query) throws IOException {
        String tableName = query.getTable();
        String whereClause = query.getWhereClause();
        String dbFilePath = connectionConfig.getDbPath();
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

        if (containColIndex(parts[1])) {
            parts[1] = parts[1].replaceAll("['\"]", "").trim();
            parts[1] = parts[1].replaceAll("col", "").trim();
            int columnIndex2 = parseColumnIndex(parts[1]);
            return dataRepository.findRecordsByCondition(tablePath, columnIndex, operator, columnIndex2);
        } else {
            String value = parts[1].replaceAll("['\"]", "").trim();
            return dataRepository.findRecordsByConstant(tablePath, columnIndex, operator, value);
        }
    }

    private boolean containColIndex(String str) {
        return str.contains("col");
    }

    private int parseColumnIndex(String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Column index must be a number: " + str);
        }
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
}