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
    List<String> schema;
    String table;
    String whereClause;
    List<Integer> columnIndices;
    List<Object> values;
    Integer rowIndex;
    String databasePath;
    String databaseName;
    String transactionName;
}