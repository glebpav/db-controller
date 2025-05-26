package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PDeleteBaseListener;
import ru.mephi.sql.parser.PDeleteParser;

public class DeleteQueryListener extends PDeleteBaseListener {
    private String tableName;
    private String whereClause;
    private boolean hasWhere = false;

    @Override
    public void enterTable_name(PDeleteParser.Table_nameContext ctx) {
        this.tableName = ctx.ID().getText();
    }

    @Override
    public void enterDelete_stmt(PDeleteParser.Delete_stmtContext ctx) {
        if (ctx.KW_WHERE() != null && ctx.condition() != null) {
            this.hasWhere = true;
            this.whereClause = ctx.condition().getText();
        }
    }

    public String getTableName() {
        return tableName;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public boolean hasWhereClause() {
        return hasWhere;
    }
}