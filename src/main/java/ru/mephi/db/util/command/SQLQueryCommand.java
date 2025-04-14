package ru.mephi.db.util.command;

import lombok.AllArgsConstructor;
import ru.mephi.db.core.executor.QueryExecutor;
import ru.mephi.db.core.parser.SQLParser;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.model.command.UserInputCommand;
import ru.mephi.db.model.command.UserInputCommandType;
import ru.mephi.db.model.query.Query;
import ru.mephi.db.model.query.QueryResult;

@AllArgsConstructor
public class SQLQueryCommand implements Command {
    SQLParser sqlParser;
    QueryExecutor queryExecutor;

    @Override
    public boolean canHandle(UserInputCommandType userInputCommandType) {
        return userInputCommandType == UserInputCommandType.SQL_QUERY;
    }

    @Override
    public void execute(String commandText) throws DatabaseException {
        Query query = sqlParser.parse(commandText);
        QueryResult result = queryExecutor.execute(query);

        // TODO: coming soon
        /* if (result.requiresStorage()) {
            fileStorageService.save(result);
        }

        loggingService.logQuery(userInput, result);*/
        System.out.println(result);
    }
}
