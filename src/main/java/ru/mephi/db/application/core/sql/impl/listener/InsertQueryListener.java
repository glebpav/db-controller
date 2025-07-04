package ru.mephi.db.application.core.sql.impl.listener;

import ru.mephi.sql.parser.PInsertBaseListener;
import ru.mephi.sql.parser.PInsert;

import java.util.ArrayList;
import java.util.List;

public class InsertQueryListener extends PInsertBaseListener {
    private String tableName;
    private final List<Object> values = new ArrayList<>();

    @Override
    public void enterTable_name(PInsert.Table_nameContext ctx) {
        this.tableName = ctx.ID().getText();
    }

    @Override
    public void enterValue(PInsert.ValueContext ctx) {
        if (ctx.NUMBER() != null) {
            String num = ctx.NUMBER().getText();
            if (num.contains(".")) {
                values.add(Double.parseDouble(num));
            } else {
                values.add(Integer.parseInt(num));
            }
        } else if (ctx.STRING() != null) {
            values.add(ctx.STRING().getText().replaceAll("^'|'$", ""));
        }
    }

    public String getTableName() {
        return tableName;
    }

    public List<Object> getValues() {
        return values;
    }
}