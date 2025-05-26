package ru.mephi.db.infrastructure.db;

import ru.mephi.db.application.adapter.db.DataRepository;
import ru.mephi.db.domain.entity.Table;

public class DataRepositoryImpl implements DataRepository {
    @Override
    public boolean tableExists(String tableName) {
        return false;
    }

    @Override
    public void saveTable(Table table) {

    }
}
