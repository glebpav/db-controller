package ru.mephi.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Column {
    private String name;
    private ColumnType columnType;
}
