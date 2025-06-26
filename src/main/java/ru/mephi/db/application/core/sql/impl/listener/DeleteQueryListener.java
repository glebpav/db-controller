package ru.mephi.db.application.core.sql.impl.listener;


import ru.mephi.sql.parser.PDeleteBaseListener;
import ru.mephi.sql.parser.PDeleteParser;

import java.util.ArrayList;
import java.util.List;

public class DeleteQueryListener extends PDeleteBaseListener {
    private String tableName;
    private Integer recordIndex;
    private final List<Integer> columnIndices = new ArrayList<>(); // Для совместимости
    private String whereClause;
    private boolean hasWhereClause = false;
    private String whereColumn;
    private String whereOperator;
    private Object whereValue;
    private String whereSecondColumn; // Для сравнения двух столбцов

    @Override
    public void enterTable_name(PDeleteParser.Table_nameContext ctx) {
        this.tableName = ctx.ID().getText();
    }

    @Override
    public void enterRow_index(PDeleteParser.Row_indexContext ctx) {
        String enterRow = ctx.NUMBER().getText();
        this.recordIndex = Integer.parseInt(enterRow);
    }

    @Override
    public void enterWhere_clause(PDeleteParser.Where_clauseContext ctx) {
        this.hasWhereClause = true;
        this.whereClause = ctx.getText();
    }

    @Override
    public void enterCompareCondition(PDeleteParser.CompareConditionContext ctx) {
        this.whereColumn = ctx.string_pattern().getText();
        this.whereOperator = ctx.comparison_operator().getText();
        this.whereValue = parseValue(ctx.value());

        if (ctx.value().column_reference() != null) {
            this.whereSecondColumn = parseColumn(ctx.value().column_reference());
        }
    }

    @Override
    public void enterLikeCondition(PDeleteParser.LikeConditionContext ctx) {
        this.whereColumn = ctx.string_pattern().get(0).getText();
        this.whereOperator = "LIKE";
        this.whereValue = ctx.string_pattern().get(1).getText().replaceAll("^'|'$", "");
    }

    @Override
    public void enterColumnCompareCondition(PDeleteParser.ColumnCompareConditionContext ctx) {
        this.whereColumn = ctx.string_pattern().get(0).getText();
        this.whereOperator = ctx.comparison_operator().getText();
        this.whereSecondColumn = ctx.string_pattern().get(1).getText();
    }

    private String parseColumn(PDeleteParser.Column_referenceContext ctx) {
        return (ctx.COLUMN_PREFIX() != null ? ctx.COLUMN_PREFIX().getText() : "") +
                ctx.NUMBER().getText();
    }

    private Object parseValue(PDeleteParser.ValueContext ctx) {
        if (ctx.STRING() != null) {
            return ctx.STRING().getText().replaceAll("^'|'$", "");
        } else if (ctx.NUMBER() != null) {
            return ctx.NUMBER().getText().contains(".")
                    ? Double.parseDouble(ctx.NUMBER().getText())
                    : Long.parseLong(ctx.NUMBER().getText());
        } else if (ctx.KW_NULL() != null) {
            return null;
        }
        throw new IllegalArgumentException("Unknown value type: " + ctx.getText());
    }

    // Геттеры
    public String getTableName() { return tableName; }
    public Integer getRecordIndex() { return recordIndex; }
    public List<Integer> getColumnIndices() { return columnIndices; }
    public boolean hasWhereClause() { return hasWhereClause; }
    public String getWhereClause() { return whereClause; }
    public String getWhereColumn() { return whereColumn; }
    public String getWhereOperator() { return whereOperator; }
    public Object getWhereValue() { return whereValue; }
    public String getWhereSecondColumn() { return whereSecondColumn; }
}