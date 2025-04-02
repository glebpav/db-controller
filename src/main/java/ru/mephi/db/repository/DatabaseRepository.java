package ru.mephi.db.repository;

import ru.mephi.db.model.Table;

public interface DatabaseRepository {

    boolean tableExists(String tableName);
    void saveTable(Table table);

}
