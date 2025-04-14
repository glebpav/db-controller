package ru.mephi.db.model.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class QueryResult {
    private final boolean success;
    private final List<Map<String, Object>> rows; // Для SELECT
    private final String message; // Для INSERT/UPDATE/DELETE
}