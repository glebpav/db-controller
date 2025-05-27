package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

public class CreateTableHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.CREATE_TABLE;
    }

    @Override
    public QueryResult handle(Query query) {
        System.out.println("Creating table: " + query.getTable());
        // Здесь реальная логика создания таблицы в БД
        return new QueryResult(true, null, "Table created successfully");
    }
}