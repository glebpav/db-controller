package ru.mephi.db.domain.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.mephi.db.domain.valueobject.ColumnType;
import ru.mephi.db.domain.valueobject.QueryType;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CreateTableQuery extends Query {

    String tableName;
    List<String> columnNames;
    List<ColumnType> columnsTypes;

    public CreateTableQuery(
            String tableName,
            List<String> columnNames,
            List<ColumnType> columnTypes
    ) {
        setType(QueryType.CREATE_TABLE);
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.columnsTypes = columnTypes;
    }

}
