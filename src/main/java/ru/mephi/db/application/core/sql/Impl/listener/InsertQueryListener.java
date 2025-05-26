package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PInsertBaseListener;
import ru.mephi.sql.parser.PInsert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertQueryListener extends PInsertBaseListener {
    private String tableName;
    private List<String> columns = new ArrayList<>();
    private List<Object> values = new ArrayList<>();


    @Override
    public void enterTable_name(PInsert.Table_nameContext ctx) {
        this.tableName = ctx.ID().getText();
    }

    @Override
    public void enterColumn_list(PInsert.Column_listContext ctx) {
        for (int i = 0; i < ctx.ID().size(); i++) {
            columns.add(ctx.ID(i).getText());
        }
    }

    @Override
    public void enterValue(PInsert.ValueContext ctx) {
        if (ctx.NUMBER() != null) {
            String num = ctx.NUMBER().getText();
            if (num.contains(".")) {
                values.add(Double.parseDouble(num));
            } else {
                values.add(Long.parseLong(num));
            }
        } else if (ctx.STRING() != null) {
            values.add(ctx.STRING().getText().replaceAll("^'|'$", ""));
        } else if (ctx.KW_NULL() != null) {
            values.add(null);
        }
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public Map<String, Object> getValuesMap() {
        if (columns.size() != values.size()) {
            throw new IllegalStateException("Number of columns doesn't match number of values");
        }

        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            result.put(columns.get(i), values.get(i));
        }
        return result;
    }

    public List<Object> getValues() {
        return values; // Было return List.of(), нужно вернуть values
    }
}