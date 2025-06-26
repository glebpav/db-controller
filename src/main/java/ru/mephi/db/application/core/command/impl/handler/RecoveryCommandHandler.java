package ru.mephi.db.application.core.command.impl.handler;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.core.command.CommandHandler;
import ru.mephi.db.application.core.TransactionManager;
import ru.mephi.db.di.qulifier.CommandPriority;
import ru.mephi.db.domain.valueobject.Priority;
import ru.mephi.db.exception.QueryExecutionException;

import javax.inject.Inject;

@CommandPriority(Priority.HIGH)
@AllArgsConstructor(onConstructor_ = @Inject)
public class RecoveryCommandHandler implements CommandHandler {
    private final TransactionManager transactionManager;

    @Override
    public boolean canHandle(String input) {
        return input != null && input.trim().toLowerCase().equals("recover");
    }

    @Override
    public void execute(String commandText) throws QueryExecutionException {
        try {
            transactionManager.recoverFromWAL();
        } catch (Exception e) {
            throw new QueryExecutionException("Recovery failed: " + e.getMessage());
        }
    }
} 