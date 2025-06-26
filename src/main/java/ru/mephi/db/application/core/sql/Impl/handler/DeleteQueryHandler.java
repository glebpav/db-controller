package ru.mephi.db.application.core.sql.Impl.handler;

import lombok.RequiredArgsConstructor;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.TransactionManager;
import ru.mephi.db.exception.LogUnableWriteTransactionException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DeleteQueryHandler implements QueryHandler {
    private final DataRepository dataRepository;
    private final TransactionManager transactionManager;

    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.DELETE;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            String tableName = query.getTable();
            String tableFilePath = transactionManager.getActualTablePath(tableName).toString();
    
            int deletedCount = 0;

            if (query.getWhereClause() != null) {
                List<Integer> matchingIndices = findMatchingIndices(query);
                deletedCount = matchingIndices.size();
                for (int i = matchingIndices.size() - 1; i >= 0; i--) {
                    int recordIndex = matchingIndices.get(i);
                    dataRepository.deleteRecord(tableFilePath, recordIndex);
                    
                    try {
                        transactionManager.logDeleteRecord(tableName, recordIndex);
                    } catch (LogUnableWriteTransactionException e) {
                        System.err.println("Warning: Failed to log delete operation: " + e.getMessage());
                    }
                }
            } else if (query.getRecordIndex() != null) {
                deletedCount = 1;
                int recordIndex = query.getRecordIndex();
                dataRepository.deleteRecord(tableFilePath, recordIndex);
                
                try {
                    transactionManager.logDeleteRecord(tableName, recordIndex);
                } catch (LogUnableWriteTransactionException e) {
                    System.err.println("Warning: Failed to log delete operation: " + e.getMessage());
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

        String tableFilePath = transactionManager.getActualTablePath(tableName).toString();

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