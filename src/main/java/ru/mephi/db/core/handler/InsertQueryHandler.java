package ru.mephi.db.core.handler;

import ru.mephi.db.model.query.Query;
import ru.mephi.db.model.query.QueryResult;
import ru.mephi.db.model.query.QueryType;

public class InsertQueryHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.INSERT;
    }

    @Override
    public QueryResult handle(Query query) {
        // TODO: implement this method
        return null;
    }
}
