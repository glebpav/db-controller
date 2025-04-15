package ru.mephi.db.usecase;

import lombok.AllArgsConstructor;
import ru.mephi.db.exception.*;
import ru.mephi.db.model.command.UserInputCommand;
import ru.mephi.db.util.command.CommandDispatcher;
import ru.mephi.db.util.command.CommandParser;

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

        } catch (DatabaseQuitException e) {
            throw e;
        } catch (DatabaseException e) {
            // TODO: implement
        }
    }
}
