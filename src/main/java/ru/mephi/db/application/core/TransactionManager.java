package ru.mephi.db.application.core;

import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.IOException;

public class TransactionManager {
    private boolean inTransaction = false;
    private final Set<String> tempTables = new HashSet<>();
    private final ConnectionConfig connectionconfig;
    

    public TransactionManager(ConnectionConfig connectionconfig) {
        this.connectionconfig = connectionconfig;
    }

    public boolean isInTransaction() {
        return inTransaction;
    }

    public void begin() {
        inTransaction = true;
    }

    public void commit() {
        inTransaction = false;
        tempTables.clear();
    }

    public void rollback() {
        inTransaction = false;
        tempTables.clear();
    }

    public void addTempTable(String tableName) {
        tempTables.add(tableName);
    }

    public Set<String> getTempTables() {
        return tempTables;
    }

    /**
     * Создаёт временную копию таблицы (например, users.txt -> users_tmp.txt)
     */
    public void createTempTableCopy(String tableName) throws IOException {
        Path originalPath = connectionconfig.getTablePath(tableName);
        Path tempPath = getTempTablePath(tableName);
        Files.copy(originalPath, tempPath, StandardCopyOption.REPLACE_EXISTING);
        addTempTable(tableName);
    }

    /**
     * Возвращает путь к временной таблице, если транзакция активна и копия существует, иначе к основной
     */
    public Path getActualTablePath(String tableName) {
        if (isInTransaction() && tempTables.contains(tableName)) {
            return getTempTablePath(tableName);
        }
        return connectionconfig.getTablePath(tableName);
    }

    /**
     * Удаляет временную таблицу (если существует)
     */
    public void deleteTempTable(String tableName) throws IOException {
        tempTables.remove(tableName);
    }

    /**
     * Возвращает путь к временной таблице (например, users.txt -> users_tmp.txt)
     */
    public Path getTempTablePath(String tableName) {
        String fileName = tableName;
        if (!fileName.endsWith(".txt")) {
            fileName += ".txt";
        }
        int dotIndex = fileName.lastIndexOf('.');
        String tempFileName = (dotIndex > 0)
                ? fileName.substring(0, dotIndex) + "_tmp" + fileName.substring(dotIndex)
                : fileName + "_tmp";
        return connectionconfig.getDbPath().resolve(tempFileName);
    }
} 