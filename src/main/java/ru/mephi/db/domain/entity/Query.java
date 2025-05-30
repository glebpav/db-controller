package ru.mephi.db.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.mephi.db.domain.valueobject.QueryType;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class Query {
    private final QueryType type;
    private final String table;
    private final Map<String, Object> data;
    // + условия WHERE, JOIN и т. д.
}

