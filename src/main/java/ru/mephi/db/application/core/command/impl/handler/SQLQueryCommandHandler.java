package ru.mephi.db.application.core.command.impl.handler;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.core.command.CommandHandler;
import ru.mephi.db.application.core.sql.QueryExecutor;
import ru.mephi.db.di.qulifier.CommandPriority;
import ru.mephi.db.domain.valueobject.Priority;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.application.core.sql.SQLParser;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;
import ru.mephi.db.infrastructure.ResultFormatter;

import javax.inject.Inject;

@CommandPriority(Priority.HIGH)
@AllArgsConstructor(onConstructor_ = @Inject)
public class SQLQueryCommandHandler implements CommandHandler {
    SQLParser sqlParser;
    QueryExecutor queryExecutor;

    @Override
    public boolean canHandle(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        String trimmed = input.trim().toLowerCase();
        return !(trimmed.equals("recover") || trimmed.equals("exit") || trimmed.equals("help"));
    }

    @Override
    public void execute(String commandText) throws DatabaseException {
        Query query = sqlParser.parse(commandText);
        QueryResult result = queryExecutor.execute(query);
        System.out.print(ResultFormatter.format(result));
    }
}
