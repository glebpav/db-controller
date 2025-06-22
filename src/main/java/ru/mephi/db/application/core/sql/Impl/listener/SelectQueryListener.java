package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PSelectBaseListener;
import ru.mephi.sql.parser.PSelect;

import java.util.ArrayList;
import java.util.List;

public class SelectQueryListener extends PSelectBaseListener {
    private String tableName;
    private final List<Integer> columnIndices = new ArrayList<>();
    private String whereClause;
    private boolean hasWhereClause = false;

    @Override
    public void enterTable_name(PSelect.Table_nameContext ctx) {
        this.tableName = ctx.ID().getText();
    }

    @Override
    public void enterColumn_index(PSelect.Column_indexContext ctx) {
        columnIndices.add(Integer.parseInt(ctx.NUMBER().getText()));
    }

    @Override
    public void enterWhere_condition(PSelect.Where_conditionContext ctx) {
        this.hasWhereClause = true;
        this.whereClause = ctx.getText();
    }

    public String getTableName() {
        return tableName;
    }

    public List<Integer> getColumnIndices() {
        return columnIndices.isEmpty() && !isSelectAll() ?
                List.of(0) : columnIndices; // По умолчанию возвращаем первую колонку
    }

    public boolean isSelectAll() {
        return columnIndices.isEmpty();
    }

    public String getWhereClause() {
        return whereClause;
    }

    public boolean hasWhereClause() {
        return hasWhereClause;
    }
}