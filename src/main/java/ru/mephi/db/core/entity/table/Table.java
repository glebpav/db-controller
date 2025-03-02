package ru.mephi.db.core.entities.table;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class Table {

    private String tableName;
    private Set<Column> columns;

}
