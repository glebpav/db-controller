package ru.mephi.db.application.core.sql.Impl.handler;

import lombok.RequiredArgsConstructor;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.ConnectionConfig;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
            int deletedCount;
            String message;
            String tablePath = connectionConfig.getDbPath() + "\\" + query.getTable() + ".txt";

            if (query.getRowIndex() != null) {

                dataRepository.deleteRecord(tablePath, query.getRowIndex());
                deletedCount = 1;
                message = String.format("Deleted row %d from %s", query.getRowIndex(), query.getTable());
            } else if (query.getWhereClause() != null) {
                List<Integer> indicesToDelete = findMatchingIndices(query);
                for (int index : indicesToDelete) {
                    dataRepository.deleteRecord(tablePath, index);
                }
                deletedCount = indicesToDelete.size();
                message = String.format("Deleted %d rows from %s where %s",
                        deletedCount, query.getTable(), query.getWhereClause());
            } else {

                List<Integer> allIndices = dataRepository.getAllRecordIndices(tablePath);
                for (int index : allIndices) {
                    dataRepository.deleteRecord(tablePath, index);
                }
                deletedCount = allIndices.size();
                message = String.format("Deleted all rows (%d) from %s", deletedCount, query.getTable());
            }

            return buildSuccessResult(deletedCount, message);
        } catch (Exception e) {
            return buildErrorResult(e);
        }
    }

    private List<Integer> findMatchingIndices(Query query) throws IOException {
        String whereClause = query.getWhereClause();
        String tablePath = connectionConfig.getDbPath() + "\\" + query.getTable() + ".txt";

        if (whereClause.contains("LIKE")) {
            String[] parts = splitCondition(whereClause, "LIKE");
            int columnIndex = Integer.parseInt(parts[0].trim());
            String pattern = cleanValue(parts[1]);
            boolean caseSensitive = !pattern.equals(pattern.toLowerCase());
            return dataRepository.findRecordsByPattern(tablePath, columnIndex, pattern, caseSensitive);
        } else {
            String operator = extractOperator(whereClause);
            String[] parts = splitCondition(whereClause, operator);
            int columnIndex = Integer.parseInt(parts[0].trim());
            String value = cleanValue(parts[1]);

            if (isNumeric(value)) {
                return dataRepository.findRecordsByCondition(tablePath, columnIndex, operator, Integer.parseInt(value));
            } else {
                return dataRepository.findRecordsByConstant(tablePath, columnIndex, operator, value);
            }
        }
    }

    private String[] splitCondition(String condition, String operator) {
        String[] parts = condition.split(operator, 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid condition format");
        }
        return parts;
    }

    private String cleanValue(String value) {
        return value.trim().replaceAll("'", "");
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    private String extractOperator(String condition) {
        if (condition.contains("!=")) return "!=";
        if (condition.contains(">=")) return ">=";
        if (condition.contains("<=")) return "<=";
        if (condition.contains("=")) return "=";
        if (condition.contains(">")) return ">";
        if (condition.contains("<")) return "<";
        throw new IllegalArgumentException("Unknown operator in condition: " + condition);
    }

    private QueryResult buildSuccessResult(int deletedCount, String message) {
        return QueryResult.builder()
                .success(true)
                .rows(Collections.singletonList(
                        Collections.singletonMap("deleted_rows", deletedCount)
                ))
                .message(message)
                .build();
    }

    private QueryResult buildErrorResult(Exception e) {
        return QueryResult.builder()
                .success(false)
                .rows(Collections.emptyList())
                .message("DELETE failed: " + e.getMessage())
                .build();
    }
}