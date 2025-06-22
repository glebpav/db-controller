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
        try {
            String message;
            int deletedCount = 1; // В реальной реализации должно быть фактическое количество

            if (query.getRowIndex() != null) {
                message = String.format("Deleted row %d from %s",
                        query.getRowIndex(), query.getTable());
            } else if (query.getWhereClause() != null) {
                message = String.format("Deleted from %s where %s",
                        query.getTable(), query.getWhereClause());
                deletedCount = 1; // В реальной реализации должно быть фактическое количество
            } else {
                message = String.format("Deleted all rows from %s", query.getTable());
                deletedCount = 1; // В реальной реализации должно быть фактическое количество
            }

            return QueryResult.builder()
                    .success(true)
                    .rows(Collections.singletonList(
                            Collections.singletonMap("deleted_rows", deletedCount)
                    ))
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