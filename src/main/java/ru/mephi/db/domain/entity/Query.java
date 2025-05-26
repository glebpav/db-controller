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
    List<String> columns;
    String whereClause;
    Map<String, Object> data;
    String transactionName;
    String databaseName; // Добавляем новое поле
}