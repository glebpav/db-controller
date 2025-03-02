package ru.mephi.db.core.usecase;

import lombok.AllArgsConstructor;
import ru.mephi.db.adapter.repository.DatabaseRepository;
import ru.mephi.db.core.entity.table.Column;
import ru.mephi.db.core.entity.table.Table;

import java.util.Set;

@AllArgsConstructor
public class CreateTableUseCase {

    private final DatabaseRepository repository;

    public void execute(String tableName, Set<Column> columns) {
        if (repository.tableExists(tableName)) {
            throw new RuntimeException("Table already exists.");
        }
        Table newTable = new Table(tableName, columns);
        repository.saveTable(newTable);
    }
}
