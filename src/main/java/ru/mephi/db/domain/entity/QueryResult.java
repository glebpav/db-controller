package ru.mephi.db.domain.entity;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class QueryResult {
    private final boolean success;
    private final List<Map<String, Object>> rows; // Для SELECT
    private final String message; // Для INSERT / UPDATE / DELETE
}