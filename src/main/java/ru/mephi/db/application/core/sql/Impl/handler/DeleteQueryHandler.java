package ru.mephi.db.application.core.sql.Impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

import java.util.Collections;

public class DeleteQueryHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.DELETE;
    }

    @Override
    public QueryResult handle(Query query) {
        System.out.println("Обработка DELETE-запроса:");
        System.out.println("Таблица: " + query.getTable());
        if (query.getWhereClause() != null) {
            System.out.println("WHERE: " + query.getWhereClause());
        }

        try {
            String message;
            if (query.getWhereClause() != null && !query.getWhereClause().isEmpty()) {
                message = String.format("Deleted from %s where %s",
                        query.getTable(), query.getWhereClause());
            } else {
                message = String.format("Deleted all rows from %s", query.getTable());
            }

            return QueryResult.builder()
                    .success(true)
                    .rows(Collections.emptyList())
                    .message(message)
                    .build();
        } catch (Exception e) {
            return QueryResult.builder()
                    .success(false)
                    .rows(Collections.emptyList())
                    .message("DELETE operation failed: " + e.getMessage())
                    .build();
        }
    }
}