package ru.mephi.db.application.core.sql.Impl;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.core.sql.SQLParser;
import ru.mephi.db.domain.entity.query.CreateTableQuery;
import ru.mephi.db.domain.valueobject.ColumnType;
import ru.mephi.db.exception.parser.NoSuchTypeException;
import ru.mephi.db.exception.parser.SQLParseException;
import ru.mephi.db.domain.entity.query.Query;
import ru.mephi.db.exception.parser.QueryParamsException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(onConstructor_ = @Inject)
public class SQLParserImpl implements SQLParser {
    @Override
    public Query parse(String sql) throws SQLParseException, NoSuchTypeException {

        var clearedRequest = sql.trim();

        if (sql.startsWith("CREATE TABLE ")) {

            clearedRequest = clearedRequest.replace("CREATE TABLE ", "");
            var keyWords = clearedRequest.split(" ");

            var tableName = keyWords[0];
            var columnNames = new ArrayList<String>();
            var columnTypes = new ArrayList<ColumnType>();

            if ((keyWords.length - 1) % 2 != 0) {
                throw new QueryParamsException();
            }

            for (int i = 0; i < keyWords.length; i++) {
                if (i % 2 == 0) {
                    columnNames.add(keyWords[i]);
                } else {
                    columnTypes.add(
                            ColumnType.getTypeBySchemaName(keyWords[i])
                    );
                }
            }

            return new CreateTableQuery(
                    tableName,
                    columnNames,
                    columnTypes
            );

        }


        throw  new SQLParseException("Query is not supported");
    }
}
