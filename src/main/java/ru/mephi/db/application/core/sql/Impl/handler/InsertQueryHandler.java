package ru.mephi.db.application.core.sql.Impl.handler;

import lombok.RequiredArgsConstructor;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;
import java.util.List;
import java.util.Map;

import ru.mephi.db.application.core.TransactionManager;

@RequiredArgsConstructor
public class InsertQueryHandler implements QueryHandler {
    private final DataRepository dataRepository;
    private final TransactionManager transactionManager;

    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.INSERT;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            String tableName = query.getTable();
            String tableFilePath = transactionManager.getActualTablePath(tableName).toString();

            List<Object> values = query.getValues();
            dataRepository.addRecord(tableFilePath, values);

            return QueryResult.builder()
                    .success(true)
                    .message(String.format("Inserted 1 row into %s", tableName))
                    .rows(List.of(Map.of(
                            "inserted_rows", 1,
                            "values", values
                    )))
                    .build();
        } catch (Exception e) {
            return QueryResult.builder()
                    .success(false)
                    .message("Failed to execute INSERT: " + e.getMessage())
                    .rows(List.of())
                    .build();
        }
    }
}