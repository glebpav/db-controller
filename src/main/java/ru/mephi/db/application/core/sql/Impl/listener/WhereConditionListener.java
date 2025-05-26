package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PWhereBaseListener;
import ru.mephi.sql.parser.PWhere;

public class WhereConditionListener extends PWhereBaseListener {
    private String whereClause;

    public String getWhereClause() {
        return whereClause;
    }

    @Override
    public void enterCondition(PWhere.ConditionContext ctx) {
        whereClause = ctx.getText();
    }
}