package ru.mephi.db.application.core.sql.impl.handler;

import ru.mephi.db.application.core.ConnectionConfig;
import ru.mephi.db.application.core.sql.QueryHandler;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.domain.valueobject.QueryType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ShowFilesHandler implements QueryHandler {
    private final ConnectionConfig connectionconfig;

    public ShowFilesHandler(ConnectionConfig connectionconfig) {
        this.connectionconfig = connectionconfig;
    }
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

            List<Map<String, String>> files = fetchFiles(dbName);

            return new QueryResult(
                    true,
                    List.of(Map.of("files", files)),
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

    private List<Map<String, String>> fetchFiles(String dbName) throws IOException {
        String dbDir = connectionconfig.getDbPath().toString();
        try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.list(java.nio.file.Paths.get(dbDir))) {
            return paths
                .filter(java.nio.file.Files::isRegularFile)
                .map(path -> {
                    try {
                        long size = java.nio.file.Files.size(path);
                        return java.util.Map.of(
                            "name", path.getFileName().toString(),
                            "size", String.valueOf(size)
                        );
                    } catch (IOException e) {
                        return java.util.Map.of(
                            "name", path.getFileName().toString(),
                            "size", "error"
                        );
                    }
                })
                .collect(java.util.stream.Collectors.toList());
        }
    }
}