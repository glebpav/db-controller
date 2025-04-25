package ru.mephi.db.application.core.sql;

import ru.mephi.db.exception.SQLParseException;
import ru.mephi.db.domain.entity.Query;

public interface SQLParser {
    Query parse(String sql) throws SQLParseException;
}
