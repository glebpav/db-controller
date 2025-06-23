package ru.mephi.db.application.core.sql.Impl.handler;

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
            List<Object> values = query.getValues();
            dataRepository.addRecord(tableFilePath, values);
            List<Map<String, Object>> resultData;

            if (query.getWhereClause() != null) {

                List<Integer> matchingIndices = findMatchingIndices(query);
                resultData = getRecordsByIndices(query.getTable(), matchingIndices, query.getColumnIndices());
            } else {
                List<Integer> allIndices = dataRepository.getAllRecordIndices(query.getTable());
                resultData = getRecordsByIndices(query.getTable(), allIndices, query.getColumnIndices());
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
        String whereClause = query.getWhereClause();
        String tablePath = query.getTable();

        if (whereClause.contains("LIKE")) {
            String[] parts = whereClause.split("LIKE");
            int columnIndex = Integer.parseInt(parts[0].trim());
            String pattern = parts[1].trim().replaceAll("'", "");
            boolean caseSensitive = !pattern.toLowerCase().equals(pattern);
            return dataRepository.findRecordsByPattern(tablePath, columnIndex, pattern, caseSensitive);
        } else if (whereClause.contains("=") || whereClause.contains(">") || whereClause.contains("<")) {

            String operator = extractOperator(whereClause);
            String[] parts = whereClause.split(operator);
            int column1 = Integer.parseInt(parts[0].trim());

            if (parts[1].trim().matches("\\d+")) { // Если второй операнд — число (индекс столбца)
                int column2 = Integer.parseInt(parts[1].trim());
                return dataRepository.findRecordsByCondition(tablePath, column1, operator, column2);
            } else { // Если второй операнд — константа (например, 'value')
                String value = parts[1].trim().replaceAll("'", "");
                return dataRepository.findRecordsByConstant(tablePath, column1, operator, value);
            }
        }
        throw new IllegalArgumentException("Unsupported WHERE condition: " + whereClause);
    }


    private String extractOperator(String condition) {
        if (condition.contains("=")) return "=";
        if (condition.contains(">")) return ">";
        if (condition.contains("<")) return "<";
        throw new IllegalArgumentException("Unknown operator in condition: " + condition);
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