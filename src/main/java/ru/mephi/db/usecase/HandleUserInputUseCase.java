package ru.mephi.db.usecase;

import lombok.AllArgsConstructor;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.DatabaseExitException;
import ru.mephi.db.exception.QueryExecutionException;
import ru.mephi.db.exception.SQLParseException;
import ru.mephi.db.model.command.UserInputCommand;
import ru.mephi.db.model.command.UserInputCommandType;
import ru.mephi.db.model.query.Query;
import ru.mephi.db.model.query.QueryResult;
import ru.mephi.db.util.command.Command;
import ru.mephi.db.util.command.CommandDispatcher;
import ru.mephi.db.util.command.CommandParser;

import java.util.List;
import java.util.Scanner;

@AllArgsConstructor
public class HandleUserInputUseCase {
    Scanner scanner;
    CommandDispatcher commandDispatcher;

    // TODO: Implement
    public void execute() throws DatabaseException {
        System.out.print("> ");
        String userInput = scanner.nextLine().trim();

        UserInputCommand userInputCommand = CommandParser.parse(userInput);

        try {

            commandDispatcher.handle(userInputCommand);

        } catch (DatabaseExitException e) {
            throw e;
        } catch (DatabaseException e) {
            // TODO: implement
        }
    }
}
