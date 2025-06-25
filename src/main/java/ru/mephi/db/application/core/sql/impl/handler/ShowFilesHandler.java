package ru.mephi.db.application.core.sql.impl.handler;

import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

import java.util.List;
import java.util.Map;

public class ShowFilesHandler implements QueryHandler {
    @Override
    public boolean canHandle(QueryType type) {
        return type == QueryType.SHOW_FILES;
    }

    @Override
    public QueryResult handle(Query query) {
        try {
            String dbName = query.getDatabaseName(); // Теперь будет работать
            String message = dbName != null ?
                    "Files for database '" + dbName + "'" : "All files";

            List<Map<String, Object>> files = fetchFiles(dbName);

            return new QueryResult(
                    true,
                    files,
                    "Found " + files.size() + " files: " + message
            );
        } catch (Exception e) {
            return new QueryResult(
                    false,
                    null,
                    "Failed to show files: " + e.getMessage()
            );
        }
    }

    private List<Map<String, Object>> fetchFiles(String dbName) {
        // Тут должна быть реализация !!!

        if (dbName != null) {
            return List.of(
                    Map.of("name", dbName + "_backup.zip", "size", "2.5MB"),
                    Map.of("name", dbName + "_data.json", "size", "1.8MB")
            );
        }
        return List.of(
                Map.of("name", "global_config.xml", "size", "128KB"),
                Map.of("name", "system_logs.log", "size", "4.2MB")
        );
    }
}