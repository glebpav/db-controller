package ru.mephi.db.util.command;

import ru.mephi.db.model.command.UserInputCommand;
import ru.mephi.db.model.command.UserInputCommandType;
import ru.mephi.db.util.Constants;

import java.util.Set;

public class CommandParser {
    public static UserInputCommand parse(String input) {
        if (input == null || input.isEmpty()) {
            return new UserInputCommand(null, UserInputCommandType.EMPTY_COMMAND);
        } else if (checkInSet(input, Constants.EXIT_COMMANDS)) {
            return new UserInputCommand(null, UserInputCommandType.EXIT_COMMAND);
        } else if (checkInSet(input, Constants.HELP_COMMANDS)) {
            return new UserInputCommand(null, UserInputCommandType.HELP_COMMAND);
        }
        return new UserInputCommand(input, UserInputCommandType.SQL_QUERY);
    }

    private static boolean checkInSet(String input, Set<String> commandsSet) {
        return commandsSet.stream().anyMatch(command -> command.equalsIgnoreCase(input));
    }
}
