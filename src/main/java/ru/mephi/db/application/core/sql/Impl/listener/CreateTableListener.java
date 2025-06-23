package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PCreateTable;
import ru.mephi.sql.parser.PCreateTableBaseListener;

import java.util.ArrayList;
import java.util.List;

public class CreateTableListener extends PCreateTableBaseListener {
    private String tableName;
    private final List<String> shema = new ArrayList<>();

    @Override
    public void enterTable_name(PCreateTable.Table_nameContext ctx) {
        this.tableName = ctx.ID().getText();
    }

    @Override
    public void enterColumn_type_list(PCreateTable.Column_type_listContext ctx) {
        for (PCreateTable.Column_typeContext typeCtx : ctx.column_type()) {
            shema.add(typeCtx.type);
        }
    }

    public String getTableName() { return tableName; }
    public List<String> getShema() { return shema; }
}