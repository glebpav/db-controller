package ru.mephi.db.repository;

import ru.mephi.db.model.Column;
import ru.mephi.db.model.Table;

import java.io.*;

public class FileDatabaseRepository implements DatabaseRepository {
    private static final String DATABASE_PATH = "path/to/database/";

    @Override
    public boolean tableExists(String tableName) {
        File file = new File(DATABASE_PATH + tableName + ".db");
        return file.exists();
    }

    @Override
    public void saveTable(Table table) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATABASE_PATH + table.getTableName() + ".db"))) {
            for (Column column : table.getColumns()) {
                writer.write(column.getName() + " " + column.getColumnType());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving table.", e);
        }
    }
}