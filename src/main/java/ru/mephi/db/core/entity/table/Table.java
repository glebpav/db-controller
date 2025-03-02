package ru.mephi.db.core.entity.table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
@Builder
public class Table {

    private String tableName;
    private Set<Column> columns;

}
