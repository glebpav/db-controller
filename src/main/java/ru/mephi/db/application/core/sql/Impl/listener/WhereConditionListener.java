package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PWhereBaseListener;
import ru.mephi.sql.parser.PWhere;

public class WhereConditionListener extends PWhereBaseListener {
    private final StringBuilder whereClause = new StringBuilder();
    private boolean needsSpace = false;

    @Override
    public void enterCondition(PWhere.ConditionContext ctx) {
        if (ctx.expr() != null) {
            // Expressions are handled in enterExpr
            return;
        }

        if (ctx.OP_AND() != null) {
            appendWithSpace("AND");
        } else if (ctx.OP_OR() != null) {
            appendWithSpace("OR");
        } else if (ctx.OP_NOT() != null) {
            appendWithSpace("NOT");
        }
    }

    @Override
    public void enterExpr(PWhere.ExprContext ctx) {
        if (ctx.OP_Equal() != null) {
            String column = ctx.column().getText();  // Получаем текст столбца
            String value = ctx.value().getText();    // Получаем текст значения
            appendComparison(column, "=", value);
        } else if (ctx.OP_NotEqual() != null) {
            appendComparison(ctx.column().getText(), "!=", ctx.value().getText());
        } else if (ctx.OP_Less() != null) {
            appendComparison(ctx.column().getText(), "<", ctx.value().getText());
        } else if (ctx.OP_More() != null) {
            appendComparison(ctx.column().getText(), ">", ctx.value().getText());
        } else if (ctx.OP_EqualLess() != null) {
            appendComparison(ctx.column().getText(), "<=", ctx.value().getText());
        } else if (ctx.OP_EqualMore() != null) {
            appendComparison(ctx.column().getText(), ">=", ctx.value().getText());
        } else if (ctx.KW_IS() != null) {
            handleIsNull(ctx);
        } else if (ctx.FC_LIKE() != null) {
            appendComparison(ctx.column().getText(), "LIKE", ctx.pattern().getText());
        }
    }

    private void handleIsNull(PWhere.ExprContext ctx) {
        appendWithSpace(ctx.column().getText());
        appendWithSpace("IS");
        if (ctx.KW_NOT() != null) {
            appendWithSpace("NOT");
        }
        appendWithSpace("NULL");
    }

    private void appendComparison(String left, String operator, String right) {
        appendWithSpace(left);
        appendWithSpace(operator);
        appendWithSpace(right);
    }

    private void appendWithSpace(String text) {
        if (needsSpace) {
            whereClause.append(" ");
        }
        whereClause.append(text);
        needsSpace = true;
    }

    public String getWhereClause() {
        return whereClause.toString();
    }
}