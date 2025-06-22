package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PDeleteBaseListener;
import ru.mephi.sql.parser.PDelete;

public class DeleteQueryListener extends PDeleteBaseListener {
    private String tableName;
    private Integer rowIndex;
    private String whereCondition;
    private boolean hasWhere = false;

    @Override
    public void enterTable_name(PDelete.Table_nameContext ctx) {
        this.tableName = ctx.ID().getText();
    }

    @Override
    public void enterRow_index(PDelete.Row_indexContext ctx) {
        this.rowIndex = Integer.parseInt(ctx.NUMBER().getText());
    }

    @Override
    public void enterWhere_condition(PDelete.Where_conditionContext ctx) {
        this.hasWhere = true;
        this.whereCondition = ctx.getText();
    }

    public String getTableName() {
        return tableName;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public String getWhereClause() {
        return whereCondition;
    }

    public boolean hasWhereClause() {
        return hasWhere;
    }

    public boolean hasRowIndex() {
        return rowIndex != null;
    }
}