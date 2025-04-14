package ru.mephi.db.core.handler;

import ru.mephi.db.model.query.Query;
import ru.mephi.db.model.query.QueryResult;
import ru.mephi.db.model.query.QueryType;

public class SelectQueryHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.SELECT;
    }

    @Override
    public QueryResult handle(Query query) {
        // TODO: select logic
        return new QueryResult(false, null, "write realization");
    }
}