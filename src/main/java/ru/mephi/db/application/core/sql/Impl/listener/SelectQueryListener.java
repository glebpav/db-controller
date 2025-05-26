package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PSelectBaseListener;
import ru.mephi.sql.parser.PSelect;

import java.util.ArrayList;
import java.util.List;

public class SelectQueryListener extends PSelectBaseListener {

    private String tableName;
    private final List<String> columns = new ArrayList<>();
    private String whereClause;
    private boolean hasWhereClause = false;

    public String getTableName() {
        return tableName;
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public boolean hasWhereClause() {
        return hasWhereClause;
    }

    @Override
    public void enterTable_name(PSelect.Table_nameContext ctx) {
        tableName = ctx.getText();
    }

    @Override
    public void enterColumn(PSelect.ColumnContext ctx) {
        columns.add(ctx.getText());
    }

    @Override
    public void enterCondition(PSelect.ConditionContext ctx) {
        hasWhereClause = true;
        whereClause = ctx.getText();
    }
}