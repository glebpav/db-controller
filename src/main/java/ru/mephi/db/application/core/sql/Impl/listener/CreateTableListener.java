package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PCreateTable;
import ru.mephi.sql.parser.PCreateTableBaseListener;

import java.util.ArrayList;
import java.util.List;

public class CreateTableListener extends PCreateTableBaseListener {
    private String tableName;
    private final List<String> columnTypes = new ArrayList<>();

    @Override
    public void enterTable_name(PCreateTable.Table_nameContext ctx) {
        tableName = ctx.getText();
    }

    @Override
    public void enterData_type(PCreateTable.Data_typeContext ctx) {
        String colType = ctx.getText().toUpperCase();
        columnTypes.add(colType);
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumnTypes() {
        return columnTypes;
    }
}