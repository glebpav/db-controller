package ru.mephi.db.application.core.sql.Impl.handler;

import lombok.RequiredArgsConstructor;
import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.entity.query.CreateTableQuery;
import ru.mephi.db.domain.entity.query.Query;
import ru.mephi.db.domain.valueobject.ColumnType;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.db.exception.QueryExecutionException;

import java.io.IOException;

@RequiredArgsConstructor
public class CreateTableQueryHandler implements QueryHandler {

    private final DataRepository dataRepository;
    private final ConnectionConfig connectionConfig;

    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.CREATE_TABLE;
    }

    @Override
    public QueryResult handle(Query query) throws QueryExecutionException {

        if (!query.getClass().equals(CreateTableQuery.class)) {
            throw new QueryExecutionException("Not perpesed querry");
        }

        var createTableQuery = (CreateTableQuery) query;

        try {
            var isTableExists = dataRepository.isTableExists(
                    connectionConfig.getDbPath(),
                    createTableQuery.getTableName()
            );

            if (isTableExists) {
                throw new QueryExecutionException("Table already exists");
            }
        } catch (IOException e) {
            throw new QueryExecutionException("Error while checking existing table", e);
        }

        try {
            dataRepository.createTableFile(
                    connectionConfig.getDbPath(),
                    createTableQuery.getTableName(),
                    createTableQuery.getColumnsTypes().stream().map(ColumnType::getSchemaName).toList()
            );
        } catch (IOException e) {
            throw new QueryExecutionException("Error while creating table file", e);
        }

        dataRepository.addTableReference(
                connectionConfig.getDbPath(),
                
        );


        return null;
    }
}
