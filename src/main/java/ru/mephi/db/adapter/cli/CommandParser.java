package ru.mephi.db.adapter.cli;

import ru.mephi.db.core.entity.table.Column;
import ru.mephi.db.core.entity.table.ColumnType;
import ru.mephi.db.core.usecase.CreateTableUseCase;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class CommandParser {
    public static void parse(String input, CreateTableUseCase createTableUseCase) {
        // Простой пример: команда "create table Users id:int name:varchar"
        if (input.startsWith("create table")) {
            String[] parts = input.substring(13).trim().split(" ");
            String tableName = parts[0];
            Set<Column> columns = new HashSet<>();
            for (int i = 1; i < parts.length; i++) {
                String[] column = parts[i].split(":");
                columns.add(new Column(column[0], ColumnType.TEXT));
            }
            createTableUseCase.execute(tableName, columns);
            System.out.println("Table " + tableName + " created successfully!");
        } else {
            throw new IllegalArgumentException("Unknown command");
        }
    }
}