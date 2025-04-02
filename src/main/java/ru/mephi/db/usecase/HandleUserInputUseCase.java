package ru.mephi.db.usecase;

import lombok.AllArgsConstructor;
import ru.mephi.db.exception.DatabaseException;
import ru.mephi.db.exception.DatabaseQuitException;

import java.util.Scanner;

@AllArgsConstructor
public class HandleUserInputUseCase {

    Scanner scanner;

    // TODO: Implement
    public void execute() throws DatabaseException {
        System.out.print("> ");
        String ignored = scanner.nextLine();
        throw new DatabaseQuitException("Exiting...");
    }
}
