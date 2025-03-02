package ru.mephi.db.adapter.repository;

import ru.mephi.db.core.entity.table.Table;

public interface DatabaseRepository {

    boolean tableExists(String tableName);
    void saveTable(Table table);

}
