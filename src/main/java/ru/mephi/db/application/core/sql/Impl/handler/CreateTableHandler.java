package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.infrastructure.db.DataRepositoryImpl;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

import java.io.IOException;
import java.util.List;

public class CreateTableHandler implements QueryHandler {
    private final DataRepositoryImpl dataRepository;

    public CreateTableHandler(DataRepositoryImpl dataRepository) {
        this.dataRepository = dataRepository;
    }

    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.CREATE_TABLE;
    }

    @Override
    public QueryResult handle(Query query) {
        String tableName = query.getTable();
        List<String> columnTypes = query.getColumnTypes();
        String dbFilePath = query.getDatabasePath();
        String dbName = query.getDatabaseName();


        String tableFilePath = dbFilePath + "/" + tableName + ".txt";
        //  dataRepository.createTableFile(tableFilePath, tableName, columnTypes);


        // dataRepository.addTableReference(dbFilePath, tableFilePath);


        return null;
    }
}