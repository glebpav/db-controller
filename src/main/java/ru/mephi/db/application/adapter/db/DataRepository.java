package ru.mephi.db.application.adapter.db;

import java.io.IOException;
import java.util.List;

public interface DataRepository {

    void createDatabaseFile(String dbFilePath, String dbName) throws IOException;
    void addTableReference(String dbFilePath, String tableFilePath) throws IOException;
    void createTableFile(String tableFilePath, String tableName, List<String> schema) throws IOException;

    void deleteDatabaseFile(String dbFilePath) throws IOException;
    void deleteTableFile(String tableFilePath) throws IOException;
    void removeTableReference(String dbFilePath, String tableFilePath) throws IOException;

    void addRecord(String tablePath, List<Object> data) throws IOException;
    List<Object> readRecord(String tablePath, int recordIndex) throws IOException;

    boolean isTableExists(String dbFilePath, String tableFilePath) throws IOException;

    public void deleteRecord(String tablePath, int recordIndex) throws IOException {
        validateTxtExtension(tablePath);
}
