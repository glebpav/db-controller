package ru.mephi.db.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.mephi.db.domain.valueobject.ColumnType;

@Data
@AllArgsConstructor
@Builder
public class Column {
    private String name;
    private ColumnType columnType;
}
