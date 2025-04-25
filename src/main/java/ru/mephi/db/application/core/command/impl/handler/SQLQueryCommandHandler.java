package ru.mephi.db.application.core.command.impl.handler;

import lombok.AllArgsConstructor;
import ru.mephi.db.application.core.command.CommandHandler;
import ru.mephi.db.application.core.sql.QueryExecutor;
import ru.mephi.db.di.qulifier.CommandPriority;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.application.core.sql.SQLParser;
import ru.mephi.db.domain.entity.Query;
import ru.mephi.db.domain.entity.QueryResult;

import javax.inject.Inject;

@CommandPriority(1)
@AllArgsConstructor(onConstructor_ = @Inject)
public class SQLQueryCommandHandler implements CommandHandler {
    SQLParser sqlParser;
    QueryExecutor queryExecutor;

    @Override
    public boolean canHandle(String input) {
        return true;
    }

    @Override
    public void execute(String commandText) throws DatabaseException {
        Query query = sqlParser.parse(commandText);
        // TODO: coming soon
        QueryResult result = queryExecutor.execute(query);

        /* if (result.requiresStorage()) {
            fileStorageService.save(result);
        }

        loggingService.logQuery(userInput, result);*/
        System.out.println(result);
    }
}
