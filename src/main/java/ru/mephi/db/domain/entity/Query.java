package ru.mephi.db.domain.entity;

import lombok.Builder;
import lombok.Value;
import ru.mephi.db.domain.valueobject.QueryType;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class Query {
    QueryType type;

    String table;
    String whereClause;
    List<Integer> columnIndices;
    List<Object> values;
    Integer rowIndex;
    List<String> columnTypes;
    String databasePath;
    String databaseName;
    String transactionName;
}