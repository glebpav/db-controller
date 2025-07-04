package ru.mephi.db.application.core.sql.impl.handler;

import lombok.RequiredArgsConstructor;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.TransactionManager;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.exception.LogUnableWriteTransactionException;

import java.io.IOException;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CreateTableHandler implements QueryHandler {
    private final DataRepository dataRepository;
    private final TransactionManager transactionManager;

    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.CREATE_TABLE;
    }

    @Override
    public QueryResult handle(Query query) {
        String tableName = query.getTable();
        List<String> schema = query.getSchema();

        String tableFilePath = transactionManager.getTempTablePath(tableName).toString();

        try {
            List<String> storageSchema = schema.stream()
                    .map(this::convertToStorageFormat)
                    .collect(Collectors.toList());

            dataRepository.createTableFile(tableFilePath, tableName, storageSchema);

            try {
                transactionManager.logCreateTable(tableName, schema);
            } catch (LogUnableWriteTransactionException e) {
                System.err.println("Warning: Failed to log table creation: " + e.getMessage());
            }

            return QueryResult.builder()
                    .success(true)
                    .message("Table created: " + tableName)
                    .rows(Collections.emptyList())
                    .build();
        } catch (IOException e) {
            return QueryResult.builder()
                    .success(false)
                    .message("Creation failed: " + e.getMessage())
                    .rows(Collections.emptyList())
                    .build();
        }
    }

    private String convertToStorageFormat(String type) {
        if (type.startsWith("str_")) {
            return "str_" + type.substring(4);
        }
        return "int";
    }
}