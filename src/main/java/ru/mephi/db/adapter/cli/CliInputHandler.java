package ru.mephi.db.adapter.cli;

import ru.mephi.db.core.usecase.CreateTableUseCase;

import javax.inject.Inject;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class CliInputHandler {

    private final CreateTableUseCase createTableUseCase;

    @Inject
    public CliInputHandler(CreateTableUseCase createTableUseCase) {
        this.createTableUseCase = createTableUseCase;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter command:");
            String input = scanner.nextLine();

            if (input.equals("exit")) {
                break;
            }

            try {
                CommandParser.parse(input, createTableUseCase);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

}
