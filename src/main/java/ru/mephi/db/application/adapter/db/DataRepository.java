package ru.mephi.db.application.adapter.db;

import ru.mephi.db.domain.entity.Table;

import java.io.IOException;

public interface DataRepository {

    void createDatabaseFile(String dbFilePath, String dbName) throws IOException;
    void addTableReference(String dbFilePath, String tableFilePath) throws IOException;
    void createTableFile(String tableFilePath, String tableName) throws IOException;
    boolean tableExists(String tableName);

}
