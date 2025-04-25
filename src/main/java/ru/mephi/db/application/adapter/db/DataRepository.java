package ru.mephi.db.application.adapter.db;

import ru.mephi.db.domain.entity.Table;

public interface DataRepository {

    boolean tableExists(String tableName);
    void saveTable(Table table);

}
