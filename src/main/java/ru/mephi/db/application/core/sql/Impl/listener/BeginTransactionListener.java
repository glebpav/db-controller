package ru.mephi.db.application.core.sql.Impl.listener;

import ru.mephi.sql.parser.PBeginTransactionBaseListener;
import ru.mephi.sql.parser.PBeginTransaction;

public class BeginTransactionListener extends PBeginTransactionBaseListener {
    private String transactionName;

    public String getTransactionName() {
        return transactionName;
    }

    @Override
    public void enterTransaction_name(PBeginTransaction.Transaction_nameContext ctx) {
        transactionName = ctx.getText();
    }
}