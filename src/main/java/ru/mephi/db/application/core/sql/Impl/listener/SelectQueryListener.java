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
    private String whereColumnIndex;
    private String whereOperator;
    private Object whereValue;
    private Integer whereSecondColumnIndex; // Для сравнения двух столбцов

    @Override
    public void enterTable_name(PSelect.Table_nameContext ctx) {
        this.tableName = ctx.ID().getText();
    }

    @Override
    public void enterColumn_list(PSelect.Column_listContext ctx) {
        for (var numCtx : ctx.NUMBER()) {
            columnIndices.add(Integer.parseInt(numCtx.getText()));
        }
    }

    @Override
    public void enterAllColumns(PSelect.AllColumnsContext ctx) {
        // Обработка SELECT *
    }

    @Override
    public void enterWhere_condition(PSelect.Where_conditionContext ctx) {
        this.hasWhereClause = true;
        this.whereClause = ctx.getText();

        // Парсинг условия WHERE
        if (ctx.expression() != null) {
            if (ctx.expression() instanceof PSelect.ColumnComparisonContext) {
                PSelect.ColumnComparisonContext compCtx = (PSelect.ColumnComparisonContext) ctx.expression();
                this.whereColumnIndex = compCtx.string_pattern().getText().replaceAll("^'|'$", "");;
                this.whereOperator = compCtx.comparison_operator().getText();
                this.whereValue = parseValue(compCtx.value());
            } else if (ctx.expression() instanceof PSelect.ColumnLikeContext) {
                PSelect.ColumnLikeContext likeCtx =
                        (PSelect.ColumnLikeContext) ctx.expression();
                this.whereColumnIndex = likeCtx.string_pattern().get(0).getText().replaceAll("^'|'$", "");
                this.whereOperator = "LIKE";
                this.whereValue = likeCtx.string_pattern().get(0).getText().replaceAll("^'|'$", "");
            }
        }
    }

    private Object parseValue(PSelect.ValueContext ctx) {
        if (ctx.STRING() != null) {
            return ctx.STRING().getText().replaceAll("^'|'$", "");
        } else if (ctx.NUMBER() != null) {
            String num = ctx.NUMBER().getText();
            return num.contains(".") ? Double.parseDouble(num) : Long.parseLong(num);
        }
        return null;
    }


    public String getTableName() { return tableName; }
    public List<Integer> getColumnIndices() { return columnIndices; }
    public boolean hasWhereClause() { return hasWhereClause; }
    public String getWhereClause() { return whereClause; }
    public String getWhereColumnIndex() { return whereColumnIndex; }
    public String getWhereOperator() { return whereOperator; }
    public Object getWhereValue() { return whereValue; }
    public Integer getWhereSecondColumnIndex() { return whereSecondColumnIndex; }
}