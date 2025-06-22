package ru.mephi.db.application.core.sql;

import ru.mephi.db.exception.parser.NoSuchTypeException;
import ru.mephi.db.exception.parser.SQLParseException;
import ru.mephi.db.domain.entity.query.Query;
import ru.mephi.db.exception.parser.QueryParamsException;

public interface SQLParser {
    Query parse(String sql) throws SQLParseException, QueryParamsException, NoSuchTypeException;
}
