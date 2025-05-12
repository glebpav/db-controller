package ru.mephi.db.infrastructure.db;

import ru.mephi.db.infrastructure.Constants;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;

@Singleton
public class DatabasePathProvider {
    private Path databasePath;

    @Inject
    public DatabasePathProvider() {}

    public void setDatabasePath(Path path) {
        this.databasePath = path;
    }

    public Path getLogFilePath() {
        return databasePath.resolve(Constants.DB_LOG_FILE);
    }
} 