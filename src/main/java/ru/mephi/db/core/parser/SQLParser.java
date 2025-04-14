package ru.mephi.db.core.parser;

import ru.mephi.db.exception.SQLParseException;
import ru.mephi.db.model.query.Query;

public interface SQLParser {
    Query parse(String sql) throws SQLParseException;
}
