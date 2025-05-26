package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PShowFilesBaseListener;
import ru.mephi.sql.parser.PShowFiles;

public class ShowFilesListener extends PShowFilesBaseListener {
    private String databaseName;

    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public void enterDb_name(PShowFiles.Db_nameContext ctx) {
        databaseName = ctx.getText();
    }
}