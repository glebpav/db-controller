package ru.mephi.db.application.core;

import java.nio.file.Path;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionConfig {

    private Path dbPath;

    // Isolation level
    // Permissions and available operations
    // etc

    /**
     * Возвращает путь к файлу таблицы по её имени внутри папки dbPath.
     * @param tableName имя таблицы
     * @return Path к файлу таблицы
     */
    public Path getTablePath(String tableName) {
        return dbPath.resolve(tableName + ".txt");
    }
}