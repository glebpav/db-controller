package ru.mephi.db.domain.valueobject;

import lombok.Getter;
import ru.mephi.db.exception.parser.NoSuchTypeException;

@Getter
public enum ColumnType {
    INTEGER("int"),
    TEXT("str_20");

    private final String schemaName;

    ColumnType(String schemaName) {
        this.schemaName = schemaName;
    }

    public static ColumnType getTypeBySchemaName(String schemaName) throws NoSuchTypeException {
        for (ColumnType columnType : ColumnType.values()) {
            if (columnType.schemaName.equals(schemaName)) {
                return columnType;
            }
        }
        throw new NoSuchTypeException("Type %s is unknown".formatted(schemaName));
    }

}
