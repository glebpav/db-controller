package ru.mephi.db.application.core.sql.Impl;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.core.sql.SQLParser;
import ru.mephi.db.exception.SQLParseException;
import ru.mephi.db.domain.entity.Query;

import javax.inject.Inject;

@AllArgsConstructor(onConstructor_ = @Inject)
public class SQLParserImpl implements SQLParser {
    @Override
    public Query parse(String sql) throws SQLParseException {
        // TODO: implement this method
        return null;
    }
}
