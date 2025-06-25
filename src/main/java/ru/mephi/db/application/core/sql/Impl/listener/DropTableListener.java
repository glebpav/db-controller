package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PDropTableBaseListener;
import ru.mephi.sql.parser.PDropTable;

public class DropTableListener extends PDropTableBaseListener {
    private String tableName;

    public String getTableName() {
        return tableName;
    }

    @Override
    public void enterTable_name(PDropTable.Table_nameContext ctx) {
        tableName = ctx.ID().getText();
    }

}