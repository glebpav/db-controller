package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PWhereBaseListener;
import ru.mephi.sql.parser.PWhere;

public class WhereConditionListener extends PWhereBaseListener {
    private final StringBuilder whereClause = new StringBuilder();

    @Override
    public void enterCondition(PWhere.ConditionContext ctx) {
        // Обрабатываем все дерево условий
        whereClause.append(ctx.getText());
    }

    public String getWhereClause() {
        return whereClause.toString();
    }
}