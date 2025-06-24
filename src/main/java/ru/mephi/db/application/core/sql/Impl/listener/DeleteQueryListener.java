package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.valueobject.QueryType;
import ru.mephi.sql.parser.PDeleteBaseListener;
import ru.mephi.sql.parser.PDelete;

public class DeleteQueryListener extends PDeleteBaseListener {
    private String tableName;
    private String whereClause;
    private Integer rowIndex;
    private boolean hasSemicolon = false;

    @Override
    public void enterTable_name(PDelete.Table_nameContext ctx) {
        this.tableName = ctx.ID().getText();
    }

    @Override
    public void enterSimpleCondition(PDelete.SimpleConditionContext ctx) {
        String columnIndex = ctx.string_pattern().STRING().getText();
        String operator = ctx.comparison_operator().getText();
        String value = getValueText(ctx.value());
        this.whereClause = columnIndex + " " + operator + " " + value;
    }

    @Override
    public void enterLikeCondition(PDelete.LikeConditionContext ctx) {
        String columnIndex = ctx.string_pattern().get(0).STRING().getText();
        String pattern = ctx.string_pattern().get(1).STRING().getText();
        this.whereClause = columnIndex + " LIKE " + pattern;
    }

    @Override
    public void enterDelete_stmt(PDelete.Delete_stmtContext ctx) {
        if (ctx.SEMICOLON() != null) {
            this.hasSemicolon = true;
        }
    }

    // Геттеры
    public String getTableName() {
        return tableName;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public boolean hasWhereClause() {
        return whereClause != null;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }

    public boolean hasRowIndex() {
        return rowIndex != null;
    }

    public boolean hasSemicolon() {
        return hasSemicolon;
    }

    private String getValueText(PDelete.ValueContext valueCtx) {
        if (valueCtx.STRING() != null) {
            return valueCtx.STRING().getText();
        } else if (valueCtx.NUMBER() != null) {
            return valueCtx.NUMBER().getText();
        } else if (valueCtx.KW_NULL() != null) {
            return "NULL";
        }
        throw new IllegalArgumentException("Unknown value type");
    }
}